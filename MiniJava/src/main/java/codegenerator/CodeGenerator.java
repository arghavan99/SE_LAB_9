package codegenerator;

import log.LogHelper;
import errorhandler.ErrorHandlerHelper;
import scanner.token.Token;
import semantic.symbol.Symbol;
import semantic.symbol.SymbolTable;
import semantic.symbol.SymbolType;

import java.util.Stack;

/**
 * Created by Alireza on 6/27/2015.
 */
public class CodeGenerator {
    private Memory memory = new Memory();
    private Stack<Address> ss = new Stack<Address>();
    private Stack<String> symbolStack = new Stack<>();
    private Stack<String> callStack = new Stack<>();
    private SymbolTable symbolTable;

    public CodeGenerator() {
        symbolTable = new SymbolTable(memory);
        //TODO
    }
    public void printMemory()
    {
        memory.pintCodeBlock();
    }
    public void semanticFunction(int func, Token next) {
        LogHelper.print("codegenerator : " + func);
        switch (func) {
            case 0:
                return;
            case 1:
                checkID();
                break;
            case 2:
                pid(next);
                break;
            case 3:
                fpid();
                break;
            case 4:
                kpid(next);
                break;
            case 5:
                intpid(next);
                break;
            case 6:
                startCall();
                break;
            case 7:
                call();
                break;
            case 8:
                arg();
                break;
            case 9:
                assign();
                break;
            case 10:
                add();
                break;
            case 11:
                sub();
                break;
            case 12:
                mult();
                break;
            case 13:
                label();
                break;
            case 14:
                save();
                break;
            case 15:
                myWhile();
                break;
            case 16:
                jpfSave();
                break;
            case 17:
                jpHere();
                break;
            case 18:
                print();
                break;
            case 19:
                equal();
                break;
            case 20:
                lessThan();
                break;
            case 21:
                and();
                break;
            case 22:
                not();
                break;
            case 23:
                defClass();
                break;
            case 24:
                defMethod();
                break;
            case 25:
                popClass();
                break;
            case 26:
                extend();
                break;
            case 27:
                defField();
                break;
            case 28:
                defVar();
                break;
            case 29:
                methodReturn();
                break;
            case 30:
                defParam();
                break;
            case 31:
                lastTypeBool();
                break;
            case 32:
                lastTypeInt();
                break;
            case 33:
                defMain();
                break;
            default:
                break;
        }
    }

    private void defMain() {
        //ss.pop();
        memory.add3AddressCode(ss.pop().getNum(), Operation.JP, new Address(memory.getCurrentCodeBlockAddress(), VarType.Address), null, null);
        String methodName = "main";
        String className = symbolStack.pop();

        symbolTable.addMethod(className, methodName, memory.getCurrentCodeBlockAddress());

        symbolStack.push(className);
        symbolStack.push(methodName);
    }

    //    public void spid(Token next){
//        symbolStack.push(next.value);
//    }
    public void checkID() {
        symbolStack.pop();
        if (ss.peek().getVarType() == VarType.Non) {
            ErrorHandlerHelper.printError("vartype none not defined ");
        }
    }

    public void pid(Token next) {
        if (symbolStack.size() > 1) {
            String methodName = symbolStack.pop();
            String className = symbolStack.pop();
            try {

                Symbol s = symbolTable.get(className, methodName, next.value);
                VarType t = VarType.Int;
                switch (s.type) {
                    case Bool:
                        t = VarType.Bool;
                        break;
                    case Int:
                        t = VarType.Int;
                        break;
                    default:
                        break;
                }
                ss.push(new Address(s.address, t));


            } catch (Exception e) {
                ss.push(new Address(0, VarType.Non));
            }
            symbolStack.push(className);
            symbolStack.push(methodName);
        } else {
            ss.push(new Address(0, VarType.Non));
        }
        symbolStack.push(next.value);
    }

    public void fpid() {
        ss.pop();
        ss.pop();

        Symbol s = symbolTable.get(symbolStack.pop(), symbolStack.pop());
        VarType t = VarType.Int;
        switch (s.type) {
            case Bool:
                t = VarType.Bool;
                break;
            case Int:
                t = VarType.Int;
                break;
            default:
                break;
        }
        ss.push(new Address(s.address, t));

    }

    public void kpid(Token next) {
        ss.push(symbolTable.get(next.value));
    }

    public void intpid(Token next) {
        ss.push(new Address(Integer.parseInt(next.value), VarType.Int, TypeAddress.Imidiate));
    }

    public void startCall() {
        //TODO: method ok
        ss.pop();
        ss.pop();
        String methodName = symbolStack.pop();
        String className = symbolStack.pop();
        symbolTable.startCall(className, methodName);
        callStack.push(className);
        callStack.push(methodName);

        //symbolStack.push(methodName);
    }

    public void call() {
        //TODO: method ok
        String methodName = callStack.pop();
        String className = callStack.pop();
        try {
            symbolTable.getNextParam(className, methodName);
            ErrorHandlerHelper.printError("The few argument pass for method");
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
            VarType t = VarType.Int;
            switch (symbolTable.getMethodReturnType(className, methodName))
            {
                case Int:
                    t = VarType.Int;
                    break;
                case Bool:
                    t = VarType.Bool;
                    break;
                default:
                    break;
            }
            memory.updateLastTempIndex();
            Address temp = new Address(memory.getTemp(),t);
            ss.push(temp);
            memory.add3AddressCode(Operation.ASSIGN, new Address(temp.getNum(), VarType.Address, TypeAddress.Imidiate), new Address(symbolTable.getMethodReturnAddress(className, methodName), VarType.Address), null);
            memory.add3AddressCode(Operation.ASSIGN, new Address(memory.getCurrentCodeBlockAddress() + 2, VarType.Address, TypeAddress.Imidiate), new Address(symbolTable.getMethodCallerAddress(className, methodName), VarType.Address), null);
            memory.add3AddressCode(Operation.JP, new Address(symbolTable.getMethodAddress(className, methodName), VarType.Address), null, null);

            //symbolStack.pop();


    }

    public void arg() {
        //TODO: method ok

        String methodName = callStack.pop();
//        String className = symbolStack.pop();
        try {
            Symbol s = symbolTable.getNextParam(callStack.peek(), methodName);
            VarType t = VarType.Int;
            switch (s.type) {
                case Bool:
                    t = VarType.Bool;
                    break;
                case Int:
                    t = VarType.Int;
                    break;
                default:
                    break;
            }
            Address param = ss.pop();
            if (param.getVarType() != t) {
                ErrorHandlerHelper.printError("The argument type isn't match");
            }
            memory.add3AddressCode(Operation.ASSIGN, param, new Address(s.address, t), null);

//        symbolStack.push(className);

        } catch (IndexOutOfBoundsException e) {
            ErrorHandlerHelper.printError("Too many arguments pass for method");
        }
        callStack.push(methodName);

    }

    public void assign() {

            Address s1 = ss.pop();
            Address s2 = ss.pop();
//        try {
            if (s1.getVarType() != s2.getVarType()) {
                ErrorHandlerHelper.printError("The type of operands in assign is different ");
            }
//        }catch (NullPointerException d)
//        {
//            d.printStackTrace();
//        }
            memory.add3AddressCode(Operation.ASSIGN, s1, s2, null);

    }

    private void addOrSub(Operation operation) {
        memory.updateLastTempIndex();
        Address temp = new Address(memory.getTemp(), VarType.Int);
        Address s2 = ss.pop();
        Address s1 = ss.pop();

        if (s1.getVarType() != VarType.Int || s2.getVarType() != VarType.Int) {
            ErrorHandlerHelper.printError("Two operands must be integer");
        }
        memory.add3AddressCode(operation, s1, s2, temp);
        ss.push(temp);
    }

    public void add() {
        addOrSub(Operation.ADD);
    }

    public void sub() {
        addOrSub(Operation.SUB);
    }

    public void mult() {
        memory.updateLastTempIndex();
        Address temp = new Address(memory.getTemp(), VarType.Int);
        Address s2 = ss.pop();
        Address s1 = ss.pop();
        if (s1.getVarType() != VarType.Int || s2.getVarType() != VarType.Int) {
            ErrorHandlerHelper.printError("In mult two operands must be integer");
        }
        memory.add3AddressCode(Operation.MULT, s1, s2, temp);
        ss.push(temp);
    }

    public void label() {
        ss.push(new Address(memory.getCurrentCodeBlockAddress(), VarType.Address));
    }

    public void save() {
        ss.push(new Address(memory.saveMemory(), VarType.Address));
    }

    public void myWhile() {
        memory.add3AddressCode(ss.pop().getNum(), Operation.JPF, ss.pop(), new Address(memory.getCurrentCodeBlockAddress() + 1, VarType.Address), null);
        memory.add3AddressCode(Operation.JP, ss.pop(), null, null);
    }

    public void jpfSave() {
        Address save = new Address(memory.saveMemory(), VarType.Address);
        memory.add3AddressCode(ss.pop().getNum(), Operation.JPF, ss.pop(), new Address(memory.getCurrentCodeBlockAddress(), VarType.Address), null);
        ss.push(save);
    }

    public void jpHere() {
        memory.add3AddressCode(ss.pop().getNum(), Operation.JP, new Address(memory.getCurrentCodeBlockAddress(), VarType.Address), null, null);
    }

    public void print() {
        memory.add3AddressCode(Operation.PRINT, ss.pop(), null, null);
    }

    public void equal() {
        memory.updateLastTempIndex();
        Address temp = new Address(memory.getTemp(), VarType.Bool);
        Address s2 = ss.pop();
        Address s1 = ss.pop();
        if (s1.getVarType() != s2.getVarType()) {
            ErrorHandlerHelper.printError("The type of operands in equal operator is different");
        }
        memory.add3AddressCode(Operation.EQ, s1, s2, temp);
        ss.push(temp);
    }

    public void lessThan() {
        memory.updateLastTempIndex();
        Address temp = new Address(memory.getTemp(), VarType.Bool);
        Address s2 = ss.pop();
        Address s1 = ss.pop();
        if (s1.getVarType() != VarType.Int || s2.getVarType() != VarType.Int) {
            ErrorHandlerHelper.printError("The type of operands in less than operator is different");
        }
        memory.add3AddressCode(Operation.LT, s1, s2, temp);
        ss.push(temp);
    }

    public void and() {
        memory.updateLastTempIndex();
        Address temp = new Address(memory.getTemp(), VarType.Bool);
        Address s2 = ss.pop();
        Address s1 = ss.pop();
        if (s1.getVarType() != VarType.Bool || s2.getVarType() != VarType.Bool) {
            ErrorHandlerHelper.printError("In and operator the operands must be boolean");
        }
        memory.add3AddressCode(Operation.AND, s1, s2, temp);
        ss.push(temp);

    }

    public void not() {
        memory.updateLastTempIndex();
        Address temp = new Address(memory.getTemp(), VarType.Bool);
        Address s2 = ss.pop();
        Address s1 = ss.pop();
        if (s1.getVarType() != VarType.Bool) {
            ErrorHandlerHelper.printError("In not operator the operand must be boolean");
        }
        memory.add3AddressCode(Operation.NOT, s1, s2, temp);
        ss.push(temp);

    }

    public void defClass() {
        ss.pop();
        symbolTable.addClass(symbolStack.peek());
    }

    public void defMethod() {
        ss.pop();
        String methodName = symbolStack.pop();
        String className = symbolStack.pop();

        symbolTable.addMethod(className, methodName, memory.getCurrentCodeBlockAddress());

        symbolStack.push(className);
        symbolStack.push(methodName);

    }

    public void popClass() {
        symbolStack.pop();
    }

    public void extend() {
        ss.pop();
        symbolTable.setSuperClass(symbolStack.pop(), symbolStack.peek());
    }

    public void defField() {
        ss.pop();
        symbolTable.addField(symbolStack.pop(), symbolStack.peek());
    }

    public void defVar() {
        ss.pop();

        String var = symbolStack.pop();
        String methodName = symbolStack.pop();
        String className = symbolStack.pop();

        symbolTable.addMethodLocalVariable(className, methodName, var);

        symbolStack.push(className);
        symbolStack.push(methodName);
    }

    public void methodReturn() {
        //TODO : call ok

        String methodName = symbolStack.pop();
        Address s = ss.pop();
        SymbolType t = symbolTable.getMethodReturnType(symbolStack.peek(), methodName);
        VarType temp = VarType.Int;
        switch (t) {
            case Int:
                break;
            case Bool:
                temp = VarType.Bool;
                break;
            default:
                break;
        }
        if (s.getVarType() != temp) {
            ErrorHandlerHelper.printError("The type of method and return address was not match");
        }
        memory.add3AddressCode(Operation.ASSIGN, s, new Address(symbolTable.getMethodReturnAddress(symbolStack.peek(), methodName), VarType.Address, TypeAddress.Indirect), null);
        memory.add3AddressCode(Operation.JP, new Address(symbolTable.getMethodCallerAddress(symbolStack.peek(), methodName), VarType.Address), null, null);

        //symbolStack.pop();

    }

    public void defParam() {
        //TODO : call Ok
        ss.pop();
        String param = symbolStack.pop();
        String methodName = symbolStack.pop();
        String className = symbolStack.pop();

        symbolTable.addMethodParameter(className, methodName, param);

        symbolStack.push(className);
        symbolStack.push(methodName);
    }

    public void lastTypeBool() {
        symbolTable.setLastType(SymbolType.Bool);
    }

    public void lastTypeInt() {
        symbolTable.setLastType(SymbolType.Int);
    }


}
