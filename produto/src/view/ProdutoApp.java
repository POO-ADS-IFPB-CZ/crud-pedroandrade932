package view;

import dao.GenericDao;
import model.Produto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Set;

public class ProdutoApp extends JFrame {

    private GenericDao<Produto> produtoDao;
    private JTable produtoTable;
    private DefaultTableModel tableModel;
    private JTextField codigoField;
    private JTextField descricaoField;
    private JTextField precoField;

    public ProdutoApp() {
        setTitle("Cadastro de Produtos");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        try {
            produtoDao = new GenericDao<>("produtos.dat");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao inicializar o DAO: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        initComponents();
        loadProdutos();
    }

    private void initComponents() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Código:"), gbc);
        gbc.gridx = 1;
        codigoField = new JTextField(15);
        inputPanel.add(codigoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Descrição:"), gbc);
        gbc.gridx = 1;
        descricaoField = new JTextField(15);
        inputPanel.add(descricaoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Preço:"), gbc);
        gbc.gridx = 1;
        precoField = new JTextField(15);
        inputPanel.add(precoField, gbc);

        String[] columnNames = {"Código", "Descrição", "Preço"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }
        };
        produtoTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(produtoTable);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)); // Add horizontal gap
        JButton adicionarButton = new JButton("Adicionar");
        JButton atualizarButton = new JButton("Atualizar");
        JButton removerButton = new JButton("Remover");

        buttonPanel.add(adicionarButton);
        buttonPanel.add(atualizarButton);
        buttonPanel.add(removerButton);

        adicionarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adicionarProduto();
            }
        });

        atualizarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                atualizarProduto();
            }
        });

        removerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removerProduto();
            }
        });

        // Layout the components
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Table selection listener to populate fields for update/delete
        produtoTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && produtoTable.getSelectedRow() != -1) {
                int selectedRow = produtoTable.getSelectedRow();
                codigoField.setText(tableModel.getValueAt(selectedRow, 0).toString());
                descricaoField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                precoField.setText(tableModel.getValueAt(selectedRow, 2).toString());
            }
        });
    }

    private void loadProdutos() {
        tableModel.setRowCount(0); // Clear existing data
        try {
            Set<Produto> produtos = produtoDao.getAll();
            for (Produto produto : produtos) {
                tableModel.addRow(new Object[]{produto.getCodigo(), produto.getDescricao(), produto.getPreco()});
            }
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar produtos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void adicionarProduto() {
        String codigo = codigoField.getText().trim();
        String descricao = descricaoField.getText().trim();
        String precoStr = precoField.getText().trim();

        if (codigo.isEmpty() || descricao.isEmpty() || precoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos os campos devem ser preenchidos.", "Erro de Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double preco;
        try {
            preco = Double.parseDouble(precoStr);
            if (preco < 0) {
                JOptionPane.showMessageDialog(this, "O preço não pode ser negativo.", "Erro de Validação", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Preço inválido. Digite um número.", "Erro de Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Produto novoProduto = new Produto(codigo, descricao, preco);
        try {
            if (produtoDao.salvar(novoProduto)) {
                JOptionPane.showMessageDialog(this, "Produto adicionado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadProdutos();
            } else {
                JOptionPane.showMessageDialog(this, "Produto com este código já existe.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar produto: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarProduto() {
        String codigo = codigoField.getText().trim();
        String descricao = descricaoField.getText().trim();
        String precoStr = precoField.getText().trim();

        if (codigo.isEmpty() || descricao.isEmpty() || precoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos os campos devem ser preenchidos.", "Erro de Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double preco;
        try {
            preco = Double.parseDouble(precoStr);
            if (preco < 0) {
                JOptionPane.showMessageDialog(this, "O preço não pode ser negativo.", "Erro de Validação", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Preço inválido. Digite um número.", "Erro de Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Produto produtoAtualizar = new Produto(codigo, descricao, preco);
        try {
            if (produtoDao.atualizar(produtoAtualizar)) {
                JOptionPane.showMessageDialog(this, "Produto atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadProdutos();
            } else {
                JOptionPane.showMessageDialog(this, "Produto com este código não encontrado para atualização.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar produto: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removerProduto() {
        String codigo = codigoField.getText().trim();

        if (codigo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o código do produto a ser removido.", "Erro de Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja remover o produto com código: " + codigo + "?", "Confirmar Remoção", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Produto produtoRemover = new Produto(codigo, "", 0); // Descrição e preço não importam para o equals/hashCode na remoção
            try {
                if (produtoDao.remover(produtoRemover)) {
                    JOptionPane.showMessageDialog(this, "Produto removido com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                    loadProdutos();
                } else {
                    JOptionPane.showMessageDialog(this, "Produto com este código não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this, "Erro ao remover produto: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields() {
        codigoField.setText("");
        descricaoField.setText("");
        precoField.setText("");
        produtoTable.clearSelection(); // Clear table selection
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ProdutoApp().setVisible(true);
            }
        });
    }
}