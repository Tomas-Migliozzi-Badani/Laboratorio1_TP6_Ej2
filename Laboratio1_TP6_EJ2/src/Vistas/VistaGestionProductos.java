package Vistas;

import Controladores.Producto;
import java.util.*;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/** 
    @author Grupo 6 
    Luis Ezequiel Sosa
    Lucas Saidman
    Gimenez Diego Ruben
    Carlos German Mecias Giacomelli
    Tomas Migliozzi Badani
    Luca Rodrigaño
    Ignacio Rodriguez
**/

public class VistaGestionProductos extends javax.swing.JInternalFrame {

    private final TreeSet<Producto> catalogo;
    private DefaultTableModel modelo;
    private Producto seleccionadoOriginal = null;

    /**
     * Creates new form VistaGestionProductos
     */
    public VistaGestionProductos(TreeSet<Producto> catalogo) {
        initComponents();
        this.catalogo = catalogo;
        this.modelo = (DefaultTableModel) tabla_productos.getModel();
        
        spiner_stock.setModel(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        
        cb_categoria.setEditable(true);
        
        actualizarCombosCategorias();
        
        if (cb_filtro_categoria.getItemCount() > 0) {
            cb_filtro_categoria.setSelectedIndex(0);
        }
        
        cargarTabla(catalogo);
        reglasHabilitacion();
        escucharCambios();
        
        cb_filtro_categoria.addItemListener(new java.awt.event.ItemListener(){
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent e){
                aplicarFiltroCategoriaActual();
            }
        });
    }

    private void escucharCambios() {
        java.awt.event.KeyAdapter ka = new java.awt.event.KeyAdapter() {
            @Override 
            public void keyReleased(java.awt.event.KeyEvent e) {
                reglasHabilitacion();
            }
        };
        
        txt_codigo.addKeyListener(ka);
        txt_descripcion.addKeyListener(ka);
        txt_precio.addKeyListener(ka);

        cb_categoria.addItemListener(new java.awt.event.ItemListener() {
            @Override 
            public void itemStateChanged(java.awt.event.ItemEvent e) {
                reglasHabilitacion();
            }
        });

        spiner_stock.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override 
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                reglasHabilitacion();
            }
        });
    }

    private void reglasHabilitacion() {
        boolean camposCompletos = 
                !txt_codigo.getText().isBlank() && 
                !txt_descripcion.getText().isBlank() && 
                !txt_precio.getText().isBlank() && 
                cb_categoria.getSelectedItem() != null && 
                !String.valueOf(cb_categoria.getSelectedItem()).trim().isEmpty();

        boolean haySeleccionTabla = tabla_productos.getSelectedRow() >= 0;

        btn_buscar.setEnabled(!txt_codigo.getText().isBlank());

        btn_eliminar.setEnabled(haySeleccionTabla);
        
        btn_guardar.setEnabled(false);
        btn_actualizar.setEnabled(false);
        
        Integer codigoActual = parseEntero(txt_codigo.getText());
        String descripcionActual = txt_descripcion.getText().trim();
        
        if(!haySeleccionTabla || seleccionadoOriginal == null){
            if(camposCompletos && codigoActual != null && !existeCodigoDescripcion(codigoActual, descripcionActual)){
                btn_guardar.setEnabled(true);
            }
            return;
        }
        
        int codigoOriginal = seleccionadoOriginal.getCodigo();
        
        if(codigoActual != null && codigoActual != codigoOriginal) {
            
            if(camposCompletos && !existeCodigoDescripcion(codigoActual, descripcionActual)){
                btn_guardar.setEnabled(true);
            }
            
            btn_actualizar.setEnabled(false);
            
        } else {
            
            Producto actual = leerFormularioSinValidar();
            boolean cambiosSinCodigo = hayCambios(seleccionadoOriginal, actual);
            
            boolean chocaConOtro = (actual != null) && existeCodigoDescripcionEnOtro(actual.getCodigo(), actual.getDescripcion(), seleccionadoOriginal);
            
            btn_actualizar.setEnabled(cambiosSinCodigo && !chocaConOtro);
            
            btn_guardar.setEnabled(false);
            
        }
    }

    private boolean hayCambios(Producto original, Producto actual) {
        if (original == null || actual == null) {
            return false;
        }
        if (!igualString(original.getDescripcion(), actual.getDescripcion())) {
            return true;
        }
        if (Double.compare(original.getPrecio(), actual.getPrecio()) != 0) {
            return true;
        }
        if (!igualString(original.getCategoria(), actual.getCategoria())) {
            return true;
        }
        if (original.getStock() != actual.getStock()) {
            return true;
        }
        return false;
    }
    
    private boolean igualString(String a, String b) {
        if(a == null && b == null){
            return true;
        }
        if(a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    private boolean existeCodigoDescripcion(int codigo, String descripcion) {
        for (Producto p : catalogo) {
            if (p.getCodigo() == codigo && p.getDescripcion().equalsIgnoreCase(descripcion)) {
                return true;
            }
        }
        return false;
    }

    private boolean existeCodigoDescripcionEnOtro(int codigo, String descripcion, Producto excluido) {
        for (Producto p : catalogo) {
            if (p != excluido && p.getCodigo() == codigo && p.getDescripcion().equalsIgnoreCase(descripcion)) {
                return true;
            }
        }
        return false;
    }

    private void cargarTabla(Collection<Producto> datos) {
        limpiarTabla();
        for (Producto p : datos) {
            modelo.addRow(new String[]{
                String.valueOf(p.getCodigo()),
                p.getDescripcion(),
                String.valueOf(p.getPrecio()),
                p.getCategoria(),
                String.valueOf(p.getStock())});
        }
        tabla_productos.clearSelection();
        seleccionadoOriginal = null;
        reglasHabilitacion();
    }

    private void limpiarTabla() {
        modelo.setRowCount(0);
    }

    private void actualizarCombosCategorias() {
        Set<String> categorias = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Producto p : catalogo) {
            if(p.getCategoria() != null) {
                String c = p.getCategoria().trim();
                if(!c.isEmpty()) {
                    categorias.add(c);
                }
            }
        }

        DefaultComboBoxModel<String> filtroModel = new DefaultComboBoxModel<String>();
        filtroModel.addElement("Todas");
        
        for (String c : categorias) {
            filtroModel.addElement(c);
        }
        
        cb_filtro_categoria.setModel(filtroModel);

        DefaultComboBoxModel<String> categoriasModel = new DefaultComboBoxModel<String>();
        
        for (String c : categorias) {
            categoriasModel.addElement(c);
        }
        
        cb_categoria.setModel(categoriasModel);
        cb_categoria.setEditable(true);
    }

    private void limpiarFormulario() {
        txt_codigo.setText("");
        txt_descripcion.setText("");
        txt_precio.setText("");
        spiner_stock.setValue(0);
        cb_categoria.setSelectedIndex(-1);
        tabla_productos.clearSelection();
        seleccionadoOriginal = null;
        reglasHabilitacion();
        txt_codigo.requestFocus();
    }

    private Producto leerFormularioSinValidar() {
        Integer codigo = parseEntero(txt_codigo.getText());
        Double precio = parseDouble(txt_precio.getText());
        
        if (codigo == null || precio == null) {
            return null;
        }
        
        String descripcion = txt_descripcion.getText().trim();
        
        Object select = cb_categoria.getSelectedItem();
        String categoria = (select == null) ? "" : select.toString().trim();
        
        int stock = (Integer) spiner_stock.getValue();
        return new Producto(codigo, descripcion, precio, stock, categoria);
    }

    private Producto leerFormularioValidando() {
        if (txt_codigo.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Complete Código");
            return null;
        }
        if (txt_descripcion.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Complete Descripción");
            return null;
        }
        if (txt_precio.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Complete Precio");
            return null;
        }
        
        Integer codigo = parseEntero(txt_codigo.getText());
        if (codigo == null) {
            JOptionPane.showMessageDialog(this, "Código inválido, solo numeros enteros");
            txt_codigo.requestFocus();
            return null;
        }
        
        Double precio = parseDouble(txt_precio.getText());
        if (precio == null) {
            JOptionPane.showMessageDialog(this, "Precio inválido, solo numeros");
            txt_precio.requestFocus();
            return null;
        }
        
        Object select = cb_categoria.getSelectedItem();
        String categoria = (select == null) ? "" : select.toString().trim();
        if (categoria.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione o ingrese una categoría.");
            cb_categoria.requestFocus();
            return null;
        }
        
        String descripcion = txt_descripcion.getText().trim();
        int stock = (Integer) spiner_stock.getValue();
        
        return new Producto(codigo, descripcion, precio, stock, categoria);
    }

    private Integer parseEntero(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Producto buscarPorCodigo(int codigo) {
        for(Producto p : catalogo) {
            if(p.getCodigo() == codigo) {
                return p;
            }
        }
        return null;
    }

    private void setFormulario(Producto p) {
        txt_codigo.setText(String.valueOf(p.getCodigo()));
        txt_descripcion.setText(p.getDescripcion());
        txt_precio.setText(String.valueOf(p.getPrecio()));
        spiner_stock.setValue(p.getStock());

        if (p.getCategoria() != null) {
            String categoria = p.getCategoria().trim();
            if(!categoria.isEmpty()){
                boolean existe = false;
                for(int i = 0; i < cb_categoria.getItemCount(); i++) {
                    String item = cb_categoria.getItemAt(i);
                    if(categoria.equalsIgnoreCase(item)){
                        existe = true;
                        break;
                    }
                }
                if(!existe) {
                    cb_categoria.addItem(categoria);
                }
                cb_categoria.setSelectedItem(categoria);
            } else {
                cb_categoria.setSelectedItem(null);
            }
        } else {
            cb_categoria.setSelectedItem(null);
        }
    }
    
    private void aplicarFiltroCategoriaActual() {
        Object select = cb_filtro_categoria.getSelectedItem();
        String categoria = (select == null) ? "Todas" : select.toString().trim();

        if ("Todas".equalsIgnoreCase(categoria)) {
            cargarTabla(catalogo);
        } else {
            ArrayList<Producto> lista = new ArrayList<Producto>();
            for (Producto p : catalogo) {
                if (p.getCategoria() != null) {
                    String c = p.getCategoria().trim();
                    if (!c.isEmpty() && c.equalsIgnoreCase(categoria)) {
                        lista.add(p);
                    }
                }
            }
            cargarTabla(lista);
        }
    }

    private void seleccionarFilaPorCodigo(int codigo) {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            Object valor = modelo.getValueAt(i, 0);
            if (valor != null && String.valueOf(codigo).equals(valor.toString())) {
                tabla_productos.setRowSelectionInterval(i, i);
                break;
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnl_gestion_productos = new javax.swing.JPanel();
        lb_titulo = new javax.swing.JLabel();
        sp_tabla_productos = new javax.swing.JScrollPane();
        tabla_productos = new javax.swing.JTable();
        lb_filtrar_categoria = new javax.swing.JLabel();
        cb_filtro_categoria = new javax.swing.JComboBox<>();
        lb_codigo = new javax.swing.JLabel();
        lb_precio = new javax.swing.JLabel();
        lb_stock = new javax.swing.JLabel();
        lb_categoria = new javax.swing.JLabel();
        lb_descripcion = new javax.swing.JLabel();
        txt_codigo = new javax.swing.JTextField();
        txt_descripcion = new javax.swing.JTextField();
        txt_precio = new javax.swing.JTextField();
        cb_categoria = new javax.swing.JComboBox<>();
        spiner_stock = new javax.swing.JSpinner();
        btn_nuevo = new javax.swing.JButton();
        btn_guardar = new javax.swing.JButton();
        btn_actualizar = new javax.swing.JButton();
        btn_eliminar = new javax.swing.JButton();
        btn_buscar = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setPreferredSize(new java.awt.Dimension(700, 700));

        lb_titulo.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lb_titulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lb_titulo.setText("Gestion de Productos");

        tabla_productos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tabla_productos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Codigo", "Descripcion", "Precio", "Categoria", "Stock"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabla_productos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabla_productosMouseClicked(evt);
            }
        });
        sp_tabla_productos.setViewportView(tabla_productos);

        lb_filtrar_categoria.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lb_filtrar_categoria.setText("Filtrar por Categoria:");

        cb_filtro_categoria.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        lb_codigo.setText("Codigo:");

        lb_precio.setText("Precio:");

        lb_stock.setText("Stock:");

        lb_categoria.setText("Categoria:");

        lb_descripcion.setText("Descripcion:");

        btn_nuevo.setIcon(new javax.swing.ImageIcon("C:\\Users\\lusai\\OneDrive\\Documentos\\ULP\\1º Año - Segundo Semestre\\Laboratorio de Programación I\\Pruebas de TP\\PruebaTP6-EJ2\\icons\\escoba.png")); // NOI18N
        btn_nuevo.setText("Nuevo");
        btn_nuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_nuevoActionPerformed(evt);
            }
        });

        btn_guardar.setIcon(new javax.swing.ImageIcon("C:\\Users\\lusai\\OneDrive\\Documentos\\ULP\\1º Año - Segundo Semestre\\Laboratorio de Programación I\\Pruebas de TP\\PruebaTP6-EJ2\\icons\\guardar.png")); // NOI18N
        btn_guardar.setText("Guardar");
        btn_guardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_guardarActionPerformed(evt);
            }
        });

        btn_actualizar.setIcon(new javax.swing.ImageIcon("C:\\Users\\lusai\\OneDrive\\Documentos\\ULP\\1º Año - Segundo Semestre\\Laboratorio de Programación I\\Pruebas de TP\\PruebaTP6-EJ2\\icons\\icons8-aprobar-y-actualizar-48.png")); // NOI18N
        btn_actualizar.setText("Actualizar");
        btn_actualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_actualizarActionPerformed(evt);
            }
        });

        btn_eliminar.setIcon(new javax.swing.ImageIcon("C:\\Users\\lusai\\OneDrive\\Documentos\\ULP\\1º Año - Segundo Semestre\\Laboratorio de Programación I\\Pruebas de TP\\PruebaTP6-EJ2\\icons\\eliminar.png")); // NOI18N
        btn_eliminar.setText("Eliminar");
        btn_eliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_eliminarActionPerformed(evt);
            }
        });

        btn_buscar.setIcon(new javax.swing.ImageIcon("C:\\Users\\lusai\\OneDrive\\Documentos\\ULP\\1º Año - Segundo Semestre\\Laboratorio de Programación I\\Pruebas de TP\\PruebaTP6-EJ2\\icons\\icons8-magnifying-glass-tilted-right-48.png")); // NOI18N
        btn_buscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_buscarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnl_gestion_productosLayout = new javax.swing.GroupLayout(pnl_gestion_productos);
        pnl_gestion_productos.setLayout(pnl_gestion_productosLayout);
        pnl_gestion_productosLayout.setHorizontalGroup(
            pnl_gestion_productosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lb_titulo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(pnl_gestion_productosLayout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addGroup(pnl_gestion_productosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnl_gestion_productosLayout.createSequentialGroup()
                        .addComponent(lb_precio, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txt_precio, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnl_gestion_productosLayout.createSequentialGroup()
                        .addComponent(lb_stock, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(spiner_stock, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnl_gestion_productosLayout.createSequentialGroup()
                        .addComponent(lb_categoria, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cb_categoria, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnl_gestion_productosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(pnl_gestion_productosLayout.createSequentialGroup()
                            .addGroup(pnl_gestion_productosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(pnl_gestion_productosLayout.createSequentialGroup()
                                    .addComponent(lb_descripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txt_descripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(pnl_gestion_productosLayout.createSequentialGroup()
                                    .addComponent(lb_codigo, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txt_codigo, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_buscar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnl_gestion_productosLayout.createSequentialGroup()
                            .addComponent(btn_nuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(btn_guardar, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(btn_actualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(btn_eliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_gestion_productosLayout.createSequentialGroup()
                .addContainerGap(21, Short.MAX_VALUE)
                .addGroup(pnl_gestion_productosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_gestion_productosLayout.createSequentialGroup()
                        .addComponent(sp_tabla_productos, javax.swing.GroupLayout.PREFERRED_SIZE, 646, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(21, 21, 21))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_gestion_productosLayout.createSequentialGroup()
                        .addComponent(lb_filtrar_categoria)
                        .addGap(18, 18, 18)
                        .addComponent(cb_filtro_categoria, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(181, 181, 181))))
        );
        pnl_gestion_productosLayout.setVerticalGroup(
            pnl_gestion_productosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_gestion_productosLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(lb_titulo)
                .addGap(18, 18, 18)
                .addGroup(pnl_gestion_productosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lb_filtrar_categoria, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cb_filtro_categoria, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(sp_tabla_productos, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addGroup(pnl_gestion_productosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnl_gestion_productosLayout.createSequentialGroup()
                        .addGroup(pnl_gestion_productosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lb_codigo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txt_codigo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(pnl_gestion_productosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lb_descripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txt_descripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(btn_buscar, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnl_gestion_productosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lb_precio, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_precio, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnl_gestion_productosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lb_categoria, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cb_categoria, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnl_gestion_productosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lb_stock, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spiner_stock, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(pnl_gestion_productosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_nuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_guardar, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_actualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_eliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(84, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnl_gestion_productos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnl_gestion_productos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_nuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_nuevoActionPerformed
        limpiarFormulario();
    }//GEN-LAST:event_btn_nuevoActionPerformed

    private void btn_guardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_guardarActionPerformed
        Producto p = leerFormularioValidando();
        if (p == null) {
            return;
        }

        if (existeCodigoDescripcion(p.getCodigo(), p.getDescripcion())) {
            JOptionPane.showMessageDialog(this, "Ya existe un producto con el mismo código y descripción.");
            return;
        }
        
        boolean ok = catalogo.add(p);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "No se pudo agregar. Verifique el código.");
            return;
        }

        actualizarCombosCategorias();
        aplicarFiltroCategoriaActual();
        limpiarFormulario();
    }//GEN-LAST:event_btn_guardarActionPerformed

    private void btn_actualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_actualizarActionPerformed
        if (seleccionadoOriginal == null) {
            return;
        }

        Producto nuevo = leerFormularioValidando();
        if (nuevo == null) {
            return;
        }

        if (nuevo.getCodigo() != seleccionadoOriginal.getCodigo()) {
            JOptionPane.showMessageDialog(this, "El código no puede cambiar al actualizar" + "\nPara crear un producto con otro codigo use 'Guardar'");
            return;
        }
        
        if (existeCodigoDescripcionEnOtro(nuevo.getCodigo(), nuevo.getDescripcion(), seleccionadoOriginal)) {
            JOptionPane.showMessageDialog(this, "Otro producto ya tiene el mismo código y descripción");
            return;
        }

        Producto quitar = null;
        for(Producto p : catalogo) {
            if(p.getCodigo() == seleccionadoOriginal.getCodigo()){
                quitar = p;
                break;
            }
        }
        
        if(quitar != null) {
            catalogo.remove(quitar);
        }
        
        catalogo.add(nuevo);
        
        actualizarCombosCategorias();
        aplicarFiltroCategoriaActual();
        
        seleccionadoOriginal = null;
        tabla_productos.clearSelection();
        limpiarFormulario();
    }//GEN-LAST:event_btn_actualizarActionPerformed

    private void btn_eliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_eliminarActionPerformed
        int fila = tabla_productos.getSelectedRow();
        if (fila < 0) {
            return;
        }

        String sCodigo = String.valueOf(modelo.getValueAt(fila, 0));
        int codigo = Integer.parseInt(sCodigo);

        Producto quitar = null;
        
        for (Producto p : catalogo) {
            if (p.getCodigo() == codigo) {
                quitar = p;
                break;
            }
        }
        
        if (quitar != null) {
            catalogo.remove(quitar);
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar");
        }

        actualizarCombosCategorias();
        aplicarFiltroCategoriaActual();

        seleccionadoOriginal = null;
        tabla_productos.clearSelection();
        limpiarFormulario();

    }//GEN-LAST:event_btn_eliminarActionPerformed

    private void btn_buscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_buscarActionPerformed
        Integer codigo = parseEntero(txt_codigo.getText());
        if (codigo == null) {
            JOptionPane.showMessageDialog(this, "Ingrese un codigo numerico.");
            txt_codigo.requestFocus();
            return;
        }
        
        Producto encontrado = buscarPorCodigo(codigo);
        if (encontrado == null) {
            JOptionPane.showMessageDialog(this, "No existe un producto con ese codigo");
            return;
        }
        
        cb_filtro_categoria.setSelectedItem("Todas");
        cargarTabla(catalogo);

        seleccionarFilaPorCodigo(codigo);
        setFormulario(encontrado);
        seleccionadoOriginal = encontrado;
        reglasHabilitacion();
    }//GEN-LAST:event_btn_buscarActionPerformed

    private void tabla_productosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabla_productosMouseClicked
        int fila = tabla_productos.getSelectedRow();
        if (fila < 0) {
            return;
        }

        String sCodigo = String.valueOf(modelo.getValueAt(fila, 0));
        int codigo = Integer.parseInt(sCodigo);
        
        Producto p = buscarPorCodigo(codigo);
        if(p != null) {
            setFormulario(p);
            seleccionadoOriginal = p;
            reglasHabilitacion();
        }
    }//GEN-LAST:event_tabla_productosMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_actualizar;
    private javax.swing.JButton btn_buscar;
    private javax.swing.JButton btn_eliminar;
    private javax.swing.JButton btn_guardar;
    private javax.swing.JButton btn_nuevo;
    private javax.swing.JComboBox<String> cb_categoria;
    private javax.swing.JComboBox<String> cb_filtro_categoria;
    private javax.swing.JLabel lb_categoria;
    private javax.swing.JLabel lb_codigo;
    private javax.swing.JLabel lb_descripcion;
    private javax.swing.JLabel lb_filtrar_categoria;
    private javax.swing.JLabel lb_precio;
    private javax.swing.JLabel lb_stock;
    private javax.swing.JLabel lb_titulo;
    private javax.swing.JPanel pnl_gestion_productos;
    private javax.swing.JScrollPane sp_tabla_productos;
    private javax.swing.JSpinner spiner_stock;
    private javax.swing.JTable tabla_productos;
    private javax.swing.JTextField txt_codigo;
    private javax.swing.JTextField txt_descripcion;
    private javax.swing.JTextField txt_precio;
    // End of variables declaration//GEN-END:variables
}
