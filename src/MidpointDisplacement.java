import javax.swing.*; // Импортируем библиотеки для создания графического интерфейса
import java.awt.*; // Импортируем библиотеки для работы с графикой
import java.awt.event.*; // Импортируем библиотеки для обработки событий
import java.awt.image.BufferedImage; // Импортируем библиотеку для работы с изображениями
import java.util.Random; // Импортируем библиотеку для генерации случайных чисел

public class MidpointDisplacement extends JPanel {

    private static final int WIDTH = 800; // Ширина окна
    private static final int HEIGHT = 600; // Высота окна
    private static final int MAX_DISPLACEMENT = 100; // Максимальное смещение для алгоритма
    private static final double INITIAL_ROUGHNESS = 0.6; // Начальное значение параметра шероховатости
    private double roughness = INITIAL_ROUGHNESS; // Текущая шероховатость
    private int[] terrain; // Массив для хранения высот ландшафта
    private BufferedImage terrainImage; // Изображение для отображения ландшафта
    private int scrollOffset = 0; // Смещение для скроллинга
    private int previousMouseX; // Предыдущее положение мыши по оси X

    public MidpointDisplacement() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT)); // Устанавливаем предпочтительный размер панели
        generateTerrain(); // Генерируем ландшафт
        addMouseListener(new MouseAdapter() { // Добавляем обработчик событий мыши
            @Override
            public void mousePressed(MouseEvent e) {
                previousMouseX = e.getX(); // Сохраняем положение мыши при нажатии
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() { // Добавляем обработчик перемещения мыши
            @Override
            public void mouseDragged(MouseEvent e) {
                int currentMouseX = e.getX(); // Текущее положение мыши
                int deltaX = currentMouseX - previousMouseX; // Разница в положении мыши
                scrollOffset = (scrollOffset - deltaX) % WIDTH; // Обновляем смещение
                previousMouseX = currentMouseX; // Обновляем предыдущее положение мыши
                repaint(); // Перерисовываем панель
            }
        });
    }

    private void generateTerrain() {
        terrain = new int[WIDTH]; // Инициализируем массив высот
        terrain[0] = HEIGHT / 2; // Начальная высота слева
        terrain[WIDTH - 1] = HEIGHT / 2; // Начальная высота справа
        midpointDisplacement(terrain, 0, WIDTH - 1, MAX_DISPLACEMENT); // Применяем алгоритм

        terrainImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB); // Создаем изображение
        Graphics2D g2d = terrainImage.createGraphics(); // Получаем объект для рисования
        for (int y = 0; y < HEIGHT; y++) { // Проходим по всем пикселям по вертикали
            for (int x = 0; x < WIDTH; x++) { // Проходим по всем пикселям по горизонтали
                if (y < terrain[x]) { // Если текущая высота меньше значения в массиве
                    g2d.setColor(new Color(0, 0, 255)); // Устанавливаем цвет для воды
                } else {
                    g2d.setColor(new Color(139, 69, 19)); // Устанавливаем цвет для песка
                }
                g2d.drawLine(x, y, x, y); // Рисуем пиксель
            }
        }
        g2d.setColor(Color.GREEN); // Устанавливаем цвет для линии высот
        for (int i = 0; i < WIDTH - 1; i++) {
            g2d.drawLine(i, terrain[i], i + 1, terrain[i + 1]); // Рисуем линию высот
        }
        g2d.dispose(); // Освобождаем ресурсы
    }

    private void midpointDisplacement(int[] terrain, int start, int end, int displacement) {
        if (end - start < 2) { // Если расстояние слишком маленькое, выходим
            return;
        }

        int mid = (start + end) / 2; // Находим среднюю точку
        Random random = new Random(); // Создаем объект для генерации случайных чисел
        terrain[mid] = (terrain[start] + terrain[end]) / 2 + (int) (random.nextGaussian() * displacement); // Смещаем среднюю точку

        midpointDisplacement(terrain, start, mid, (int) (displacement * roughness)); // Рекурсивно применяем алгоритм к левой половине
        midpointDisplacement(terrain, mid, end, (int) (displacement * roughness)); // Рекурсивно применяем алгоритм к правой половине
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Вызываем метод родительского класса
        g.drawImage(terrainImage, -scrollOffset, 0, null); // Рисуем изображение с учетом смещения
        g.drawImage(terrainImage, WIDTH - scrollOffset, 0, null); // Рисуем изображение для бесшовного скроллинга
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Midpoint Displacement"); // Создаем окно

        MidpointDisplacement panel = new MidpointDisplacement(); // Создаем панель с ландшафтом
        JPanel controlPanel = new JPanel(); // Создаем панель управления
        JLabel roughnessLabel = new JLabel("Roughness:"); // Создаем метку для параметра шероховатости
        JTextField roughnessField = new JTextField(5); // Создаем текстовое поле для ввода шероховатости
        roughnessField.setText(String.valueOf(INITIAL_ROUGHNESS)); // Устанавливаем начальное значение шероховатости
        JButton generateButton = new JButton("Generate"); // Создаем кнопку для генерации нового ландшафта

        generateButton.addActionListener(new ActionListener() { // Добавляем обработчик событий для кнопки
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    panel.roughness = Double.parseDouble(roughnessField.getText()); // Считываем новое значение шероховатости
                    panel.generateTerrain(); // Генерируем новый ландшафт
                    panel.repaint(); // Перерисовываем панель
                } catch (NumberFormatException ex) { // Обрабатываем исключение, если введено неверное значение
                    JOptionPane.showMessageDialog(frame, "Please enter a valid number for roughness.", "Error", JOptionPane.ERROR_MESSAGE); // Показываем сообщение об ошибке
                }
            }
        });

        controlPanel.add(roughnessLabel); // Добавляем метку на панель управления
        controlPanel.add(roughnessField); // Добавляем текстовое поле на панель управления
        controlPanel.add(generateButton); // Добавляем кнопку на панель управления

        frame.setLayout(new BorderLayout()); // Устанавливаем компоновку для окна
        frame.add(panel, BorderLayout.CENTER); // Добавляем панель с ландшафтом в центр окна
        frame.add(controlPanel, BorderLayout.SOUTH); // Добавляем панель управления в нижнюю часть окна
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Устанавливаем действие при закрытии окна
        frame.pack(); // Упаковываем компоненты окна
        frame.setLocationRelativeTo(null); // Центрируем окно
        frame.setVisible(true); // Делаем окно видимым
    }
}
