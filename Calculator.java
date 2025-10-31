package com.example.calculator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.function.BiFunction;

public class Calculator extends JFrame {
    private final JTextField display = new JTextField("0");
    private BigDecimal current = BigDecimal.ZERO;
    private BigDecimal stored = null;
    private BiFunction<BigDecimal, BigDecimal, BigDecimal> op = null;
    private boolean clearOnNextDigit = true;

    public Calculator() {
        super("Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(320, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(6,6));

        display.setEditable(false);
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
        add(display, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(5,4,6,6));
        String[] buttons = {
                "C","±","%","/",
                "7","8","9","*",
                "4","5","6","-",
                "1","2","3","+",
                "0",".","<-","="
        };
        for(String b : buttons){
            JButton btn = new JButton(b);
            btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
            btn.addActionListener(e -> onPress(b));
            grid.add(btn);
        }
        add(grid, BorderLayout.CENTER);

        // Keyboard support
        addKeyBindings();
    }

    private void addKeyBindings() {
        String[][] map = {
                {"0","0"},{"1","1"},{"2","2"},{"3","3"},{"4","4"},
                {"5","5"},{"6","6"},{"7","7"},{"8","8"},{"9","9"},
                {".","."},{"ENTER","="},{"EQUALS","="},{"ADD","+"},{"SUBTRACT","-"},
                {"MULTIPLY","*"},{"DIVIDE","/"},{"BACK_SPACE","<-"},
        };
        JComponent root = getRootPane();
        for(String[] m : map){
            root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(m[0]), m[1]);
            root.getActionMap().put(m[1], new AbstractAction(){
                @Override public void actionPerformed(ActionEvent e){ onPress(m[1]); }
            });
        }
    }

    private void onPress(String key){
        try {
            switch (key) {
                case "C":
                    current = BigDecimal.ZERO;
                    stored = null;
                    op = null;
                    clearOnNextDigit = true;
                    display.setText("0");
                    break;
                case "±":
                    current = current.negate();
                    display.setText(strip(current.toPlainString()));
                    break;
                case "%":
                    current = current.divide(BigDecimal.valueOf(100));
                    display.setText(strip(current.toPlainString()));
                    break;
                case "<-":
                    if(!clearOnNextDigit){
                        String s = display.getText();
                        if (s.length() > 1) s = s.substring(0, s.length()-1);
                        else s = "0";
                        current = new BigDecimal(s);
                        display.setText(strip(s));
                    }
                    break;
                case ".":
                    if (clearOnNextDigit) {
                        display.setText("0.");
                        clearOnNextDigit = false;
                    } else if (!display.getText().contains(".")) {
                        display.setText(display.getText() + ".");
                    }
                    break;
                case "+": case "-": case "*": case "/":
                    commitPending();
                    stored = current;
                    op = operatorFor(key);
                    clearOnNextDigit = true;
                    break;
                case "=":
                    commitPending();
                    op = null;
                    stored = null;
                    clearOnNextDigit = true;
                    break;
                default: // digits
                    if (clearOnNextDigit) {
                        display.setText(key);
                        clearOnNextDigit = false;
                    } else {
                        display.setText(strip(display.getText() + key));
                    }
                    current = new BigDecimal(display.getText());
            }
        } catch (ArithmeticException ex){
            display.setText("Error");
            current = BigDecimal.ZERO;
            stored = null;
            op = null;
            clearOnNextDigit = true;
        } catch (Exception ex) {
            display.setText("Error");
        }
    }

    private void commitPending(){
        // If there is a pending operation, execute it with stored op current
        if (op != null && stored != null) {
            if(op == Calculator::div && current.compareTo(BigDecimal.ZERO) == 0){
                throw new ArithmeticException("Division by zero");
            }
            current = op.apply(stored, current);
            display.setText(strip(current.stripTrailingZeros().toPlainString()));
        }
    }

    private static String strip(String s){
        if(s.contains(".")){
            // remove trailing zeros after decimal
            s = new BigDecimal(s).stripTrailingZeros().toPlainString();
        }
        return s;
    }

    private static BiFunction<BigDecimal, BigDecimal, BigDecimal> operatorFor(String key){
        switch (key){
            case "+": return Calculator::add;
            case "-": return Calculator::sub;
            case "*": return Calculator::mul;
            case "/": return Calculator::div;
            default: return null;
        }
    }
    private static BigDecimal add(BigDecimal a, BigDecimal b){ return a.add(b); }
    private static BigDecimal sub(BigDecimal a, BigDecimal b){ return a.subtract(b); }
    private static BigDecimal mul(BigDecimal a, BigDecimal b){ return a.multiply(b); }
    private static BigDecimal div(BigDecimal a, BigDecimal b){ return a.divide(b, 12, java.math.RoundingMode.HALF_UP); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Calculator().setVisible(true));
    }
}