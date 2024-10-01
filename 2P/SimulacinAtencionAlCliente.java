import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SimulacinAtencionAlCliente extends JFrame {

    private JTextField cedulaField;
    private JComboBox<String> categoriaComboBox;
    private JComboBox<String> servicioComboBox;
    private JTextField horaLlegadaField;
    private JSlider tiempoAtencionSlider;
    private JLabel tiempoAtencionLabel;
    private DefaultListModel<String> colaModel;
    private JList<String> colaList;
    private JButton registrarButton;
    private JButton guardarButton;
    private JButton atenderButton; // Botón para atender paciente
    private File archivoLog;
    private JLabel pacienteActualLabel; // Etiqueta para mostrar el paciente actual

    public SimulacinAtencionAlCliente() {
        // Configuración del layout principal
        setLayout(new BorderLayout());

        // Crear panel superior para ingresar datos del paciente
        JPanel panelSuperior = new JPanel(new GridLayout(4, 2));
        inicializarCampos(panelSuperior);

        add(panelSuperior, BorderLayout.NORTH);

        // Crear panel inferior para mostrar la cola y control deslizante
        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));
        inicializarPanelInferior(panelInferior);

        add(panelInferior, BorderLayout.CENTER);

        // Configurar eventos
        configurarEventos();

        // Inicializar el archivo de log
        inicializarArchivoLog();

        // Configurar ventana principal
        configurarVentana();

        // Inicializar el Timer para actualizar la hora de llegada
        iniciarTimer();
    }

    private void inicializarCampos(JPanel panelSuperior) {
        cedulaField = new JTextField();
        categoriaComboBox = new JComboBox<>(new String[]{"Adulto Mayor", "Persona con Discapacidad", "Menor de 10 años", "Persona Gestante", "Personal General"});
        servicioComboBox = new JComboBox<>(new String[]{"Consulta Médica General", "Consulta Médica Especializada", "Prueba de Laboratorio", "Cita médica Prioritaria", "Odontologia", "Pediatria"});
        horaLlegadaField = new JTextField();
        horaLlegadaField.setEditable(false); // Hacer que el campo no sea editable por el usuario

        panelSuperior.add(new JLabel("Cédula:"));
        panelSuperior.add(cedulaField);
        panelSuperior.add(new JLabel("Categoría:"));
        panelSuperior.add(categoriaComboBox);
        panelSuperior.add(new JLabel("Servicio Solicitado:"));
        panelSuperior.add(servicioComboBox);
        panelSuperior.add(new JLabel("Hora de Llegada:"));
        panelSuperior.add(horaLlegadaField);
    }

    private void inicializarPanelInferior(JPanel panelInferior) {
        colaModel = new DefaultListModel<>();
        colaList = new JList<>(colaModel);
        JScrollPane scrollPaneCola = new JScrollPane(colaList);

        tiempoAtencionSlider = new JSlider(JSlider.HORIZONTAL, 1, 60, 15);
        tiempoAtencionSlider.setMajorTickSpacing(10);
        tiempoAtencionSlider.setMinorTickSpacing(5);
        tiempoAtencionSlider.setPaintTicks(true);
        tiempoAtencionSlider.setPaintLabels(true);

        tiempoAtencionLabel = new JLabel("Tiempo de Atención: 15 minutos");

        registrarButton = new JButton("Registrar Paciente");
        guardarButton = new JButton("Guardar Log");
        atenderButton = new JButton("Atender Paciente"); // Crear el botón para atender paciente

        pacienteActualLabel = new JLabel("Paciente Actual: Ninguno"); // Inicializar la etiqueta del paciente actual

        panelInferior.add(scrollPaneCola);
        panelInferior.add(tiempoAtencionSlider);
        panelInferior.add(tiempoAtencionLabel);
        panelInferior.add(registrarButton);
        panelInferior.add(atenderButton); // Añadir el botón para atender paciente
        panelInferior.add(guardarButton);
        panelInferior.add(pacienteActualLabel); // Añadir etiqueta al panel inferior
    }

    private void configurarEventos() {
        registrarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarPaciente();
            }
        });

        guardarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarLog();
            }
        });

        atenderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                atenderPaciente(); // Llamar al método para atender al paciente
            }
        });

        tiempoAtencionSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                actualizarTiempoAtencion();
            }
        });
    }

    private void inicializarArchivoLog() {
        archivoLog = new File("log_atencion_cliente.txt");
        try {
            if (!archivoLog.exists()) {
                archivoLog.createNewFile();
            }
        } catch (IOException ex) {
            mostrarError("Error al crear o abrir el archivo de log.");
        }
    }

    private void configurarVentana() {
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void registrarPaciente() {
        String cedula = cedulaField.getText();
        String categoria = (String) categoriaComboBox.getSelectedItem();
        String servicio = (String) servicioComboBox.getSelectedItem();
        String horaLlegada = horaLlegadaField.getText();

        if (!cedula.isEmpty() && !categoria.isEmpty() && !servicio.isEmpty() && !horaLlegada.isEmpty()) {
            colaModel.addElement(cedula + " - " + categoria + " - " + servicio + " - " + horaLlegada);
            actualizarTiempoAtencion();
            cedulaField.setText("");
        } else {
            mostrarAdvertencia("Por favor, complete todos los campos.");
        }
    }

    private void atenderPaciente() {
        // Verificar si hay pacientes en la cola
        if (!colaModel.isEmpty()) {
            String paciente = colaModel.remove(0); // Obtener y eliminar el primer paciente de la lista
            pacienteActualLabel.setText("Paciente Actual: " + paciente); // Actualizar la etiqueta del paciente actual
        } else {
            mostrarAdvertencia("No hay pacientes en la lista de espera.");
        }
    }

    private void guardarLog() {
        try (FileWriter writer = new FileWriter(archivoLog, true)) {
            for (int i = 0; i < colaModel.size(); i++) {
                writer.write(colaModel.get(i) + "\n");
            }
            writer.flush();
            mostrarInformacion("Log guardado correctamente.");
        } catch (IOException ex) {
            mostrarError("Error al guardar el log.");
        }
    }

    private void actualizarTiempoAtencion() {
        int valorSlider = tiempoAtencionSlider.getValue();
        tiempoAtencionLabel.setText("Tiempo de Atención: " + valorSlider + " minutos");
    }

    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarInformacion(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void iniciarTimer() {
        Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // Obtener la hora actual del sistema
                String horaActual = new SimpleDateFormat("HH:mm:ss").format(new Date());
                // Actualizar el campo de texto con la hora actual
                horaLlegadaField.setText(horaActual);
            }
        };
        // Programar el Timer para que se ejecute cada segundo
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SimulacinAtencionAlCliente();
            }
        });
    }
}
