package httl.ast;

/**
 * @author icode
 */
public class ImportDirective extends LineDirective {
    private String[] imports;

    public ImportDirective(String[] imports, int offset) {
        super(offset);
        this.imports = imports;
    }

    public String[] getImports() {
        return imports;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String s : imports) {
            builder.append(s).append(",");
        }
        builder.deleteCharAt(builder.length() - 2);
        return "#import(" + builder.toString() + ")";
    }
}
