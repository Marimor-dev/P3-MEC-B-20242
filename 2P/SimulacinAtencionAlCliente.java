import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;
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
    private File archivoLog;

    private Timer timer; // Timer para atención de pacientes
    private Timer horaTimer; // Timer para mostrar la hora
    private int pacientesAtendidos = 0; // Contador de pacientes atendidos
    private final int MAX_PACIENTES = 10; // Número máximo de pacientes en cola
    private boolean atendiendo = false; // Estado de atención

    public SimulacinAtencionAlCliente() {
        setLayout(new BorderLayout());

        JPanel panelSuperior = new JPanel(new GridLayout(4, 2));
        inicializarCampos(panelSuperior);
        add(panelSuperior, BorderLayout.NORTH);

        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));
        inicializarPanelInferior(panelInferior);
        add(panelInferior, BorderLayout.CENTER);

        configurarEventos();
        inicializarArchivoLog();
        configurarVentana();
        iniciarHoraTimer(); // Cambiado a iniciarHoraTimer
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

        tiempoAtencionLabel = new JLabel("Tiempo de Atención: 15 segundos"); // Cambiado a segundos

        registrarButton = new JButton("Registrar Paciente");
        guardarButton = new JButton("Guardar Log");

        panelInferior.add(scrollPaneCola);
        panelInferior.add(tiempoAtencionSlider);
        panelInferior.add(tiempoAtencionLabel);
        panelInferior.add(registrarButton);
        panelInferior.add(guardarButton);
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

        if (!cedula.isEmpty() && !categoria.isEmpty() && !servicio.isEmpty()) {
            colaModel.addElement(cedula + " - " + categoria + " - " + servicio + " - " + horaLlegada);
            actualizarTiempoAtencion();
            cedulaField.setText("");

            // Iniciar atención si se alcanzan 10 pacientes
            if (colaModel.size() >= MAX_PACIENTES && !atendiendo) {
                atendiendo = true; // Cambiar a estado de atención
                atenderPacientes();
            }
        } else {
            mostrarAdvertencia("Por favor, complete todos los campos.");
        }
    }

    private void atenderPacientes() {
        // Programar un timer para atender a los pacientes
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (pacientesAtendidos < colaModel.size()) {
                    String pacienteAtendido = colaModel.getElementAt(pacientesAtendidos);
                    mostrarInformacion("Atendiendo: " + pacienteAtendido);
                    pacientesAtendidos++;
                    colaModel.remove(0); // Remover el paciente atendido de la cola

                    // Verificar si ya atendimos a 10 pacientes
                    if (pacientesAtendidos >= MAX_PACIENTES) {
                        atendiendo = false; // Volver al estado no atendiendo
                        timer.cancel(); // Detener el timer
                    }
                }
            }
        }, 0, tiempoAtencionSlider.getValue() * 1000); // Atender cada 'tiempo de atención' segundos
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
        tiempoAtencionLabel.setText("Tiempo de Atención: " + valorSlider + " segundos");
    }

    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarInformacion(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void iniciarHoraTimer() {
        // Timer para mostrar la hora actual en el campo de hora de llegada
        horaTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                String horaActual = new SimpleDateFormat("HH:mm:ss").format(new Date());
                horaLlegadaField.setText(horaActual);
            }
        };

        horaTimer.scheduleAtFixedRate(timerTask, 0, 1000);
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
