
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.lang.*;
//number before bracket 

/**
 * This class uses two stacks to evaluate an infix arithmetic expression from an
 * InputStream.
 */
public class InfixExpressionEvaluator {
    // Tokenizer to break up our input into tokens
    StreamTokenizer tokenizer;

    // Stacks for operators (for converting to postfix) and operands (for
    // evaluating)
    String postFix = "";     
    StackInterface<Character> operators;
    StackInterface<Double> operands;
    /**
     * Initializes the solver to read an infix expression from input.
     */
    public InfixExpressionEvaluator(InputStream input) {
        // Initialize the tokenizer to read from the given InputStream
        tokenizer = new StreamTokenizer(new BufferedReader(
                        new InputStreamReader(input)));

        // Declare that - and / are regular characters (ignore their regex
        // meaning)
        tokenizer.ordinaryChar('-');
        tokenizer.ordinaryChar('/');

        // Allow the tokenizer to recognize end-of-line
        tokenizer.eolIsSignificant(true);

        // Initialize the stacks
        operators = new ArrayStack<Character>();
        operands = new ArrayStack<Double>();
    }

    /**
     * A type of runtime exception thrown when the given expression is found to
     * be invalid
     */
    class ExpressionError extends RuntimeException {
        ExpressionError(String msg) {
            super(msg);
        }
    }

    /**
     * Creates an InfixExpressionEvaluator object to read from System.in, then
     * evaluates its input and prints the result.
     */
    public static void main(String[] args) {
        InfixExpressionEvaluator solver =
                        new InfixExpressionEvaluator(System.in);
        Double value = solver.evaluate();
        if (value != null) {
            System.out.println(value);
        }
    }

    /**
     * Evaluates the expression parsed by the tokenizer and returns the
     * resulting value.
     */
    public Double evaluate() throws ExpressionError {
        // Get the first token. If an IO exception occurs, replace it with a
        // runtime exception, causing an immediate crash.
        try {
            tokenizer.nextToken();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Continue processing tokens until we find end-of-line
        while (tokenizer.ttype != StreamTokenizer.TT_EOL) {
            // Consider possible token types
            switch (tokenizer.ttype) {
                case StreamTokenizer.TT_NUMBER:
                    // If the token is a number, process it as a double-valued
                    // operand
                    processOperand((double)tokenizer.nval);
                    break;
                case '+':
                case '-':
                case '*':
                case '/':
                case '^':
                    // If the token is any of the above characters, process it
                    // is an operator
                    processOperator((char)tokenizer.ttype);
                    break;
                case '(':
                case '[':
                    // If the token is open bracket, process it as such. Forms
                    // of bracket are interchangeable but must nest properly.
                    processOpenBracket((char)tokenizer.ttype);
                    break;
                case ')':
                case ']':
                    // If the token is close bracket, process it as such. Forms
                    // of bracket are interchangeable but must nest properly.
                    processCloseBracket((char)tokenizer.ttype);
                    break;
                case StreamTokenizer.TT_WORD:
                    // If the token is a "word", throw an expression error
                    throw new ExpressionError("Unrecognized token: " +
                                    tokenizer.sval);
                default:
                    // If the token is any other type or value, throw an
                    // expression error
                    throw new ExpressionError("Unrecognized token: " +
                                    String.valueOf((char)tokenizer.ttype));
            }
            // Read the next token, again converting any potential IO exception
            try {
                tokenizer.nextToken();
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Almost done now, but we may have to process remaining operators in
        // the operators stack
        processRemainingOperators();

        // Return the result of the evaluation
        // TODO: Fix this return statement
        return operands.pop();
    }

    /**
     * Processes an operand.
     */
    void processOperand(double operand) {
        operands.push(operand);//adds next number to the stack
        postFix += Double.toString(operand);

    }

    /**
     * Processes an operator.
     */
    void processOperator(char operator) {
        postFix += (operator);
        boolean flag = false;
        for(int i= 1; i<postFix.length(); i++){//tests if multiple operators in series and throws error 
            if(postFix.charAt(i)=='+'||postFix.charAt(i)=='-'||postFix.charAt(i)=='/'||postFix.charAt(i)=='^'||postFix.charAt(i)=='*'){
                if(postFix.charAt(i-1)=='+'||postFix.charAt(i-1)=='-'||postFix.charAt(i-1)=='/'||postFix.charAt(i-1)=='^'||postFix.charAt(i-1)=='*')
                    throw new ExpressionError("multiple operators in series");
            }
        }
        for(int i= 0; i<postFix.length(); i++){
            if(Character.isDigit(postFix.charAt(i)))
                flag =true;//sets flag true if number is present
        }
        if(!flag)
            throw new ExpressionError("no operands present");
        //ends test for no numbers present
        
        double temp1 = 0;
        double temp2 = 0;
        char topOperator;       
        if(operator == '^'){
            operators.push(operator);//adds carrot to operator
        }
        else{
            while(!operators.isEmpty() && precedence(operator)<=precedence(operators.peek())&& (operators.peek() !='(' && operators.peek()!='[')){//preforms operator at top of stack if not empty and precedence is right
                temp1= operands.pop();
                temp2 = operands.pop();
                topOperator = operators.pop();
                switch(topOperator){
                    case '+':
                        System.out.println("Addition with:"+temp1+ "+" + temp2+ "="+(temp1+temp2));
                        operands.push(temp1+temp2);
                        break;
                    case '-':
                        operands.push(temp2-temp1);
                        System.out.println("Subtraction with:"+temp2+ "-" + temp1+ "="+(temp2-temp1));                        
                        break;
                    case '*':
                        operands.push(temp1*temp2);
                        System.out.println("Multiplication with:"+temp1+ "*" + temp2+ "="+(temp1*temp2));
                        break;
                    case '^':
                        operands.push(Math.pow(temp2,temp1));
                        System.out.println("Power with:"+temp2+ "^" + temp1+ "="+(Math.pow(temp2,temp1)));
                        break;
                    case '/':
                        if(temp1==0){
                            throw new ExpressionError("divide by 0 error");
                        }
                        operands.push(temp2/temp1);
                        System.out.println("Divide with:"+temp2+ "/" + temp1+ "="+(temp2/temp1));
                        break;                   
                }                
            } 
           operators.push(operator);
        }
    }

    /**
     * Processes an open bracket.
     */
    void processOpenBracket(char openBracket) {
        boolean flag = false;
        postFix += (openBracket);
        operators.push(openBracket);//push new bracket onto the stack operators
    }

    /**
     * Processes a close bracket.
     */
    void processCloseBracket(char closeBracket) {
        postFix += (closeBracket);
        boolean flag3 = false;
        for(int i= 0; i<postFix.length(); i++){
            if(Character.isDigit(postFix.charAt(i)))
                flag3 =true;//sets flag true if number is present
        }
        if(!flag3){
            throw new ExpressionError("no operands present");
        }
        boolean flag2 = false;
        for(int i= 0; i<postFix.length(); i++){
            if(postFix.charAt(i)=='('||postFix.charAt(i)=='[')
                flag2 = true;
        }
        if(!flag2)
            throw new ExpressionError("no open brackets found");
        //end open bracket error detection 
        
        boolean flag = false;
        char matchedBracket;
        char unmatchedBracket = '(';
        if(closeBracket==')'){
            matchedBracket = '(';
            unmatchedBracket = '[';
        }
        else
            matchedBracket= '[';
        //sets up matching brackets 
        for(int i= 0; i<postFix.length(); i++){
            if(postFix.charAt(i)==matchedBracket)
                flag= true;
        }
        if(!flag)
            throw new ExpressionError("no open matching bracket");
        //end no bracket error check
        
        char topOperator = operators.pop();
        double temp1;
        double temp2;
        
        while(topOperator != matchedBracket){
        //while (topOperator != '('&& topOperator != '{'){
            temp1= operands.pop();
            temp2 = operands.pop();
            switch(topOperator){
                case '+':
                    operands.push(temp1+temp2);
                    System.out.println("Addition with:"+temp1+ "+" + temp2+ "="+(temp1+temp2));
                    break;
                case '-':
                    operands.push(temp2-temp1);
                    System.out.println("Subtraction with:"+temp2+ "-" + temp1+ "="+(temp2-temp1));
                    break;
                case '*':
                    operands.push(temp1*temp2);
                    System.out.println("Multiplication with:"+temp1+ "*" + temp2+ "="+(temp1*temp2));
                    break;
                case '^':
                    operands.push(Math.pow(temp2,temp1));
                    System.out.println("Power with:"+temp2+ "^" + temp1+ "="+(Math.pow(temp2,temp1)));
                    break;
                case '/':
                    if(temp1==0){
                        throw new ExpressionError("divide by 0 error");
                    }
                    operands.push(temp2/temp1);
                    System.out.println("Divide with:"+temp2+ "/" + temp1+ "="+(temp2/temp1));
                    break;                   
                }
            topOperator = operators.pop();
            if(topOperator == unmatchedBracket){
                throw new ExpressionError("Unmatched bracket error");
            }
        }
    }

    /**
     * Processes any remaining operators leftover on the operators stack
     */
    void processRemainingOperators() {
        char topOperator;
        double temp1;
        double temp2;
        while(!operators.isEmpty()){
            temp1= operands.pop();
            temp2 = operands.pop();
            topOperator = operators.pop();
            switch(topOperator){
                case '+':
                    operands.push(temp1+temp2);
                    System.out.println("Addition with:"+temp1+ "+" + temp2+ "="+(temp1+temp2));
                    break;
                case '-':
                    operands.push(temp2-temp1);
                    System.out.println("Subtraction with:"+temp2+ "-" + temp1+ "="+(temp2-temp1));
                    break;
                case '*':
                    operands.push(temp1*temp2);
                    System.out.println("Multiplication with:"+temp1+ "*" + temp2+ "="+(temp1*temp2));
                    break;
                case '^':
                    operands.push(Math.pow(temp2,temp1));
                    System.out.println("Power with:"+temp2+ "^" + temp1+ "="+(Math.pow(temp2,temp1)));
                    break;
                case '/':
                    if(temp1==0){
                        throw new ExpressionError("divide by 0 error");
                    }
                    operands.push(temp2/temp1);
                    System.out.println("Divide with:"+temp2+ "/" + temp1+ "="+(temp2/temp1));
                    break;
                }                      
        }
        System.out.println("This is what the computer used as your entry: "+ postFix);

    }
    //returns num value for precedence of the operator passed
    //higher precedence = higher value
    int precedence(char operator){
        if(operator =='+' || operator == '-')
                return 1;
        else if(operator =='*'|| operator == '/')
                return 2;
        else if(operator == '^')
                return 3;
        else
            return 4;//gives brackets higher precedence 
                 
        }
}



