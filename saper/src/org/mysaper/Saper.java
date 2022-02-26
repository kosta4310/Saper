package org.mysaper;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.LinkedList;

public class Saper extends JFrame implements ActionListener{
    private JPanel pane;/*главная панель*/
    private int[][] sup;/*массив с цифрами*/
    private LinkedList<String> bombList;/*содержит список бомб*/
    private LinkedList<JButton> buttonList;/*список кнопок*/
    private LinkedList<int[]> listOfNullCell;
    private LinkedList<String> listOfOpenedCell;
    private JComponent[][] components;/*массив компонентов для отображения*/
    LinkedList<String> buttonsWithFlag;
    JPanel gridCell;
    JLabel timerLabel;
    JLabel countLabel;
    Timer timer;
    long lastTickTock;
    boolean isEnd = false;


    public Saper() throws HeadlessException {
        super("Saper_from_PavlovK");
        buttonList = new LinkedList<>();
        components = new JComponent[9][9];

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createAndShow();
        add(pane);
        setResizable(false);
        pack();
        setVisible(true);
        setLayout(null);
        setLocationRelativeTo(null);
    }

    private void createAndShow() {
        pane = new JPanel();
        pane .setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBounds(5, 5, buttonPanel.getWidth() - 10,
                buttonPanel.getHeight() - 10);
        buttonPanel.setBorder(new LineBorder(Color.RED));


        gridCell = new JPanel(new GridLayout(9, 9));

        addingButtonsToGrid();

        buttonPanel.add(gridCell);

        JPanel countTimerStartPanel = new JPanel(new FlowLayout());
        countTimerStartPanel.setBounds(5, 5, buttonPanel.getWidth() - 10,
                buttonPanel.getHeight() - 10);
        countTimerStartPanel.setBorder(new LineBorder(Color.RED));


        countLabel = new JLabel(String.format("%03d", 0));
        countLabel.setFont(new Font("Arial", Font.BOLD, 30));

        JButton startButton = new JButton();
        startButton.setActionCommand("start");
        startButton.setIcon(createIcon("images/bomb.png"));
        startButton.addActionListener(this);


        timerLabel = new JLabel(String.format("%03d", 0));
        timerLabel.setFont(new Font("Arial", Font.BOLD, 30));

        countTimerStartPanel.add(countLabel);
        countTimerStartPanel.add(startButton);
        countTimerStartPanel.add(timerLabel);

        pane.add(countTimerStartPanel);
        pane.add(buttonPanel);
    }

    private JButton createButton(int i, int j) {

        return getSimpleButton(i,j);
    }

    private JButton getSimpleButton(int i, int j) {
        JButton button = new JButton();
        button.setActionCommand(""+i+j);
        buttonList.add(button);
        Dimension size = new Dimension(20, 20);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setMinimumSize(size);
        return button;
    }

    private static ImageIcon createIcon(String path) {
        URL imgURL = Saper.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("File not found " + path);
            return null;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand().equals("start")) {
            if (isEnd||(timer!=null&& timer.isRunning())) {
                gridCell.removeAll();
                addingButtonsToGrid();
                gridCell.repaint();
                gridCell.revalidate();
            }
            start();

        } else {
            String s = e.getActionCommand();
            int x = (Integer.valueOf(s))/10;
            int y=(Integer.valueOf(s))%10;
            userInput(x, y);
            fillGridComponents();
        }
    }

    private void addingButtonsToGrid() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                components[i][j] = createButton(i, j);
                gridCell.add(components[i][j]);
            }
        }
    }

    private JLabel createLabel(int i, int j) {
        JLabel lab;
        if (sup[i][j]!=-1) {
            String s = ""+sup[i][j];
            lab = getSimpleLabel();
            lab.setText(s);
            lab.setHorizontalAlignment(JLabel.CENTER);
        } else {
            lab = createLabRed();
        }

        return lab;
    }

    private void start() {
        buttonsWithFlag = new LinkedList<>();
        listOfNullCell = new LinkedList<>();
        listOfOpenedCell=new LinkedList<>();
        ActionListener listener=new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long running = System.currentTimeMillis() - lastTickTock;
                long seconds = running / 1000;
                timerLabel.setText(String.format("%03d", seconds));
            }
        };
        lastTickTock = System.currentTimeMillis();
        timer = new Timer(500, listener);
        if (!timer.isRunning())timer.start();
        countLabel.setText(String.format("%03d", 10));
        initialize();
    }

    private void initialize() {
        sup = new int[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                sup[i][j] = 0;
            }
        }
        /*расстановка бомб*/

        boolean isGoog;
        bombList = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            isGoog = false;
            while (!isGoog) {
                int k = (int) (Math.random() * 9);
                int l = (int) (Math.random() * 9);
                if (!bombList.contains("" + k + l)) {
                    sup[k][l] = -1;
                    bombList.add("" + k + l);
                    isGoog = true;
                }
            }
        }
        /*ищем бомбы и расставляем вокруг них цифры*/
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (sup[i][j]==-1) {
                    for (int k = -1; k < 2; k++) {
                        for (int l = -1; l < 2; l++) {
                            setNumbersCell(i + k, j + l);
                        }
                    }
                }
            }
        }
        for (JButton button :
                buttonList) {
            button.addActionListener(this);
            button.addMouseListener(new SetRemoveFlag());
        }
    }

    /*метод проверяет ячейку по переданным  координатам и расставляет цифры
    ,проверяет координаты на валидность*/
    void setNumbersCell(int i, int j) {
        if (i >= 0 && i < 9 && j >= 0 && j < 9) {
            if (sup[i][j]!=-1) sup[i][j] += 1;
        }
    }
    private void userInput(int x, int y) {

        openCell(x, y);
        /*значение переданной ячейки*/
        if ( sup[x][y]!= 0) {

            if (sup[x][y] == -1) createExplosion(x,y);

        } else {

            listOfNullCell.add(new int[]{x, y});

            while (!listOfNullCell.isEmpty()) {
                int[] temp = new int[2];
                temp = listOfNullCell.removeFirst();
                int xx = temp[0];
                int yy = temp[1];
                for (int k = -1; k < 2; k++) {
                    for (int l = -1; l < 2; l++) {
                        checkAdjacentCells(xx + k, yy + l);
                    }
                }
            }
        }
    }
    private void checkAdjacentCells(int i, int j) {
        if (i >= 0 && i < 9 && j >= 0 && j < 9) {
            if (!listOfOpenedCell.contains("" + i + j)) {
                openCell(i, j);
                if (sup[i][j] == 0) listOfNullCell.add(new int[]{i, j});
            }
        }
    }
    private void openCell(int x, int y) {

        listOfOpenedCell.add(""+x+y);
    }
    void createExplosion(int x, int y) {
        components[x][y] = createLabRed();
        timer.stop();
        isEnd = true;
        removeListeners();
        bombList.remove("" + x + y);
        while (!bombList.isEmpty()) {
            String s = bombList.remove();
            if (!buttonsWithFlag.contains(s)) {
                int i = (Integer.valueOf(s))/10;
                int j=(Integer.valueOf(s))%10;
                JLabel lab = getSimpleLabel();
                lab.setIcon(createIcon("images/bomb.png"));
                lab.setHorizontalAlignment(JLabel.CENTER);
                components[i][j] = lab;
            }else{buttonsWithFlag.remove(s);}
        }
        while (!buttonsWithFlag.isEmpty()) {
            String s = buttonsWithFlag.remove();
                int i = (Integer.valueOf(s))/10;
                int j=(Integer.valueOf(s))%10;
                JLabel label = getSimpleLabel();
                label.setIcon(createIcon("images/Xbomb20x20.png"));
                components[i][j] = label;
        }
        new Thread(()->{
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            JOptionPane.showMessageDialog(this,"YOU LOOSER"
                    ,"ПОПРОБУЙТЕ ЕЩЕ РАЗ"
                    ,JOptionPane.INFORMATION_MESSAGE);
        }).start();
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Saper gui = new Saper();

            }
        });

    }
    private JLabel createLabRed() {
        JLabel label = getSimpleLabel();
        label.setOpaque(true);
        label.setBackground(Color.RED);
        label.setIcon(createIcon("images/bomb.png"));
        return label;
    }

    private JLabel getSimpleLabel() {
        JLabel label = new JLabel();
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder()
        ));

        Dimension size = new Dimension(20, 20);
        label.setPreferredSize(size);
        label.setMaximumSize(size);
        label.setMinimumSize(size);
        return label;
    }
    class SetRemoveFlag extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                JButton button1 = (JButton) e.getComponent();
                String s = button1.getActionCommand();
                int x = (Integer.valueOf(s))/10;
                int y=(Integer.valueOf(s))%10;

                if (!buttonsWithFlag.contains(s)) {
                    buttonsWithFlag.add(s);
                    JButton button = (JButton)components[x][y] ;
                    button.setIcon(createIcon("images/flag20x20.png"));
                    components[x][y]= button;
                    countLabel.setText(String.format("%03d", 10-buttonsWithFlag.size()));
                } else {
                    buttonsWithFlag.remove(s);
                    JButton button=(JButton) components[x][y];
                    button.setIcon(null);
                    components[x][y] = button;
                    countLabel.setText(String.format("%03d", 10-buttonsWithFlag.size()));
                }

                fillGridComponents();
            }
        }
    }

    private void fillGridComponents() {
        gridCell.removeAll();
        int stayAliveButtons=0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (listOfOpenedCell.contains("" + i + j)) {
                    gridCell.add(createLabel(i, j));
                } else{
                    gridCell.add(components[i][j]);
                    stayAliveButtons++;
                }
            }
        }
        gridCell.repaint();
        gridCell.revalidate();
        if (stayAliveButtons == buttonsWithFlag.size()) {
            isEnd = true;
            timer.stop();
            removeListeners();
            new Thread(()->{
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                JOptionPane.showMessageDialog(this,"YOU WON"
                        ,"congratulations from the Pavlov K."
                        ,JOptionPane.INFORMATION_MESSAGE);
            }).start();
        }
    }

    private void removeListeners() {
        for (JButton button :
                buttonList) {
            button.removeActionListener(this);
            for (MouseListener adapter :
                    button.getMouseListeners()) {
                button.removeMouseListener(adapter);
            }
        }
    }
}
