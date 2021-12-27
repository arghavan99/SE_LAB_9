package codegenerator;

/**
 * Created by mohammad hosein on 6/28/2015.
 */
public class Address {
    private int num;
    private TypeAddress Type;
    private VarType varType;

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public TypeAddress getType() {
        return Type;
    }

    public void setType(TypeAddress type) {
        Type = type;
    }

    public VarType getVarType() {
        return varType;
    }

    public void setVarType(VarType varType) {
        this.varType = varType;
    }

    public Address(int num, VarType varType, TypeAddress Type) {
       setNum(num);
       setType(Type);
       setVarType(varType);
    }

    public Address(int num, VarType varType) {
        setNum(num);
        setVarType(varType);
        setType(TypeAddress.Direct);
    }

    public String toString(){
        switch (Type){
            case Direct:
                return num+"";
            case Indirect:
                return "@"+getNum();
            case Imidiate:
                return "#"+getNum();
            default:
                return getNum()+"";
        }
    }
}
