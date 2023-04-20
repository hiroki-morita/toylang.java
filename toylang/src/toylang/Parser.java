package toylang;

import java.util.ArrayList;
import java.util.List;

/**
 * 式の再帰下降パーサ
 * 
 * ## 文法
 * 
 * Expr -> AddSubExpr
 * 
 * AddSubExpr -> MulDivExpr ((ADDOP|SUBOP) MulDivExpr)?
 * MulDivExpr -> Primary ((MULOP|DIVOP) Primary)?
 * Primary -> LPAREN Expr RPAREN | INT
 */
public class Parser {
    private final List<Token> toks;

    private int pos;

    public Parser(Lexer lexer) {
        this.toks = new ArrayList<>();
        this.pos = 0;

        Token tok = null;
        do {
            tok = lexer.next();
            this.toks.add(tok);
        } while (tok.kind() != Token.Kind.EOF);
    }

    public Expr parse() {
        return expr();
    }

    private Expr expr() {
        return addSubExpr();
    }

    private Expr addSubExpr() {
        final var l = mulDivExpr();
        var tok = toks.get(pos);
        // MulDivExpr ADDOP MulDivExpr
        if (tok.kind() == Token.Kind.ADDOP) {
            pos++; // consume ADDOP
            final var r = mulDivExpr();
            return new Expr.BinOp(Expr.BinOp.Kind.ADD, l, r);
        }
        // MulDivExpr SUBOP MulDivExpr
        if (tok.kind() == Token.Kind.SUBOP) {
            pos++; // consume ADDOP
            final var r = mulDivExpr();
            return new Expr.BinOp(Expr.BinOp.Kind.SUB, l, r);
        }
        // MulDivExpr
        else {
            return l;
        }
    }

    private Expr mulDivExpr() {
        final var l = primary();
        var tok = toks.get(pos);
        // Primary MULOP Primary
        if (tok.kind() == Token.Kind.MULOP) {
            pos++; // consume MULOP
            final var r = primary();
            return new Expr.BinOp(Expr.BinOp.Kind.MUL, l, r);
        }
        // Primary DIVOP Primary
        if (tok.kind() == Token.Kind.DIVOP) {
            pos++; // consume DIVOP
            final var r = primary();
            return new Expr.BinOp(Expr.BinOp.Kind.DIV, l, r);
        }
        // Primary
        else {
            return l;
        }
    }

    private Expr primary() {
        var tok = toks.get(pos);
        // LPAREN Expr RPAREN
        if (tok.kind() == Token.Kind.LPAREN) {
            pos++; // consume LPAREN
            final var e = expr();
            pos++; // consume RPAREN
            return e;
        }
        // INT
        if (tok.kind() == Token.Kind.INT) {
            pos++; // consume INT
            final var intLit = (Token.Int) (tok);
            return new Expr.Int(intLit.n);
        }
        // error!
        throw new RuntimeException("unknown token: " + tok);
    }

}
