package com.yusudhanseslihesapla.seslihesapla;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TextView textScreen;
    private ImageView btnSpeak;
    private Button btnDelete;
    private boolean lastNumeric;
    private boolean stateError;
    private boolean lastDot;

    private int[] numericButtons = {
            R.id.btnSifir,
            R.id.btnBir,
            R.id.btnIki,
            R.id.btnUc,
            R.id.btnDort,
            R.id.btnBes,
            R.id.btnAlti,
            R.id.btnYedi,
            R.id.btnSekiz,
            R.id.btnDokuz,
    };

    private int[] operateButtons = {
            R.id.btnPlus,
            R.id.btnMines,
            R.id.btnMult,
            R.id.btnDevi,
            R.id.btnParantezSol,
            R.id.btnParantezSag,
    };

    private final int REQ_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Objects.requireNonNull(getSupportActionBar()).hide();

        btnSpeak = findViewById(R.id.btnSpeak);
        textScreen = findViewById(R.id.textScreen);
        btnDelete = findViewById(R.id.btnDelete);

        setNumericOnClickListener();
        setOperatorOnClickListener();

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!stateError) {
                    String expression = textScreen.getText().toString();
                    if (!expression.isEmpty()) {
                        expression = expression.substring(0, expression.length() - 1);
                        textScreen.setText(expression);
                        lastNumeric = false;
                        lastDot = false;
                    }
                }
            }
        });
    }
    private void setOperatorOnClickListener() {
        View.OnClickListener operatorListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastNumeric && !stateError) {
                    Button button = (Button) v;
                    textScreen.append(button.getText());
                    lastNumeric = false;
                    lastDot = false;
                }
            }
        };

        for (int id : operateButtons) {
            findViewById(id).setOnClickListener(operatorListener);
        }

        findViewById(R.id.btnParantezSol).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textScreen.append("(");
                lastNumeric = false;
                lastDot = false;
            }
        });

        findViewById(R.id.btnParantezSag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastNumeric && !stateError && hasOpenParentheses()) {
                    textScreen.append(")");
                    lastNumeric = true;
                    lastDot = false;
                }
            }
        });

        findViewById(R.id.btnClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textScreen.setText("");
                lastNumeric = false;
                stateError = false;
                lastDot = false;
            }
        });

        findViewById(R.id.btnEquals).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opEqual();
            }
        });

        findViewById(R.id.btnSpeak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stateError) {
                    textScreen.setText("Yeniden Deneyin");
                    stateError = false;
                } else {
                    speachInput();
                }
                lastNumeric = true;
            }
        });
    }
    private boolean hasOpenParentheses() {
        String text = textScreen.getText().toString();
        int openParenthesesCount = 0;
        int closeParenthesesCount = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '(') {
                openParenthesesCount++;
            } else if (ch == ')') {
                closeParenthesesCount++;
            }
        }
        return openParenthesesCount > closeParenthesesCount;
    }
    private void setNumericOnClickListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;

                if (stateError) {
                    textScreen.setText(button.getText());
                    stateError = false;
                } else {
                    textScreen.append(button.getText());
                }
                lastNumeric = true;
                lastDot = false;
            }
        };

        for (int id : numericButtons) {
            findViewById(id).setOnClickListener(listener);
        }

        findViewById(R.id.btnDot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastNumeric && !stateError && !lastDot) {
                    textScreen.append(".");
                    lastNumeric = false;
                    lastDot = true;
                }
            }
        });

        findViewById(R.id.btnVirgul).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastNumeric && !stateError && !lastDot) {
                    textScreen.append(".");
                    lastNumeric = false;
                    lastDot = true;
                }
            }
        });
    }
    private void speachInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_promot));

        try {
            startActivityForResult(intent, REQ_CODE);
        } catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void opEqual() {
        if (lastNumeric && !stateError) {
            String expression = textScreen.getText().toString();

            try {
                double result = evaluateExpression(expression);
                textScreen.setText(String.valueOf(result));
            } catch (IllegalArgumentException e) {
                textScreen.setText("Hata");
            }
        }
    }
    private double evaluateExpression(String expression) {
        Expression e = new ExpressionBuilder(expression).build();
        try {
            return e.evaluate();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Hata");
        }
    }

    /*private String addParentheses(String expression) {
        StringBuilder sb = new StringBuilder();
        int parenthesesCount = 0;
        boolean lastCharWasNumber = false;

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                sb.append(c);
                lastCharWasNumber = true;
            } else {
                if (lastCharWasNumber && parenthesesCount > 0) {
                    sb.append(")");
                    parenthesesCount--;
                }
                sb.append(c);
                lastCharWasNumber = false;
                if (c == '(') {
                    parenthesesCount++;
                }
            }
        }

        while (parenthesesCount > 0) {
            sb.append(")");
            parenthesesCount--;
        }

        return sb.toString();
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> voiceResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String change = voiceResults.get(0);

                    textScreen.setText(voiceResults.get(0));

                    openResponse(voiceResults.get(0));

                    change = change.replace("böl", "/");
                    change = change.replace("bölü", "/");

                    change = change.replace("X", "*");
                    change = change.replace("x", "*");

                    change = change.replace("artı", "+");
                    change = change.replace("ekle", "+");

                    change = change.replace("eksi", "-");
                    change = change.replace("çıkart", "-");

                    change = change.replace("parantez", "(");
                    change = change.replace("Parantez", ")");

                    while (change.contains("Parantez")) {
                        change = change.replaceFirst("Parantez", ")");}

                    double result;
                    try {
                        result = evaluateExpression(change);
                        textScreen.setText(String.valueOf(result));
                    } catch (IllegalArgumentException e) {
                        textScreen.setText("Geçersiz ifade");
                    }
                }
                break;
        }
    }
    private List<String> infixToPostfix(String expression) {
        List<String> postfixExpression = new ArrayList<>();
        Deque<String> operatorStack = new ArrayDeque<>();

        String[] tokens = expression.split("\\s+");
        for (String token : tokens) {
            if (isNumeric(token)) {
                postfixExpression.add(token);
            } else if (isOperator(token)) {
                while (!operatorStack.isEmpty() && isOperator(operatorStack.peek()) && hasHigherPrecedence(operatorStack.peek(), token)) {
                    postfixExpression.add(operatorStack.pop());
                }
                operatorStack.push(token);
            } else if (token.equals("(")) {
                operatorStack.push(token);
            } else if (token.equals(")")) {
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                    postfixExpression.add(operatorStack.pop());
                }
                if (!operatorStack.isEmpty() && operatorStack.peek().equals("(")) {
                    operatorStack.pop();
                } else {
                    throw new IllegalArgumentException("Geçersiz ifade");
                }
            }
        }

        while (!operatorStack.isEmpty()) {
            String operator = operatorStack.pop();
            if (operator.equals("(")) {
                throw new IllegalArgumentException("Geçersiz ifade");
            }
            postfixExpression.add(operator);
        }

        return postfixExpression;
    }
    private boolean isNumeric(String token) {
        return token.matches("\\d+(\\.\\d+)?");
    }
    private boolean isOperator(String token) {
        return token.matches("[+\\-*/]");
    }
    private boolean hasHigherPrecedence(String operator1, String operator2) {
        if ((operator1.equals("*") || operator1.equals("/")) && (operator2.equals("+") || operator2.equals("-"))) {
            return true;
        }
        return false;
    }
    private double evaluatePostfix(List<String> postfixExpression) {
        Deque<Double> operandStack = new ArrayDeque<>();

        for (String token : postfixExpression) {
            if (isNumeric(token)) {
                operandStack.push(Double.parseDouble(token));
            } else if (isOperator(token)) {
                if (operandStack.size() < 2) {
                    throw new IllegalArgumentException("Geçersiz ifade");
                }
                double operand2 = operandStack.pop();
                double operand1 = operandStack.pop();
                double result = performOperation(operand1, operand2, token);
                operandStack.push(result);
            } else {
                throw new IllegalArgumentException("Geçersiz ifade");
            }
        }

        if (operandStack.size() != 1) {
            throw new IllegalArgumentException("Geçersiz ifade");
        }

        return operandStack.pop();
    }
    private double performOperation(double operand1, double operand2, String operator) {
        switch (operator) {
            case "+":
                return operand1 + operand2;
            case "-":
                return operand1 - operand2;
            case "*":
                return operand1 * operand2;
            case "/":
                if (operand2 == 0) {
                    throw new IllegalArgumentException("Geçersiz ifade");
                }
                return operand1 / operand2;
            default:
                throw new IllegalArgumentException("Geçersiz ifade");
        }
    }
    private void openResponse(String msg) {
        String msgs = msg.toLowerCase(Locale.ROOT);

        if (msgs.indexOf("aç") != -1) {
            if (msgs.indexOf("google") != -1) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/"));
                startActivity(intent);
            }
            if (msgs.indexOf("chrome") != -1) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/"));
                startActivity(intent);
            }
            if (msgs.indexOf("youtube") != -1) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/"));
                startActivity(intent);
            }
            if (msgs.indexOf("twitter") != -1) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.twitter.com/"));
                startActivity(intent);
            }
            if (msgs.indexOf("facebook") != -1) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/"));
                startActivity(intent);
            }
        }
    }
}
