package toylang;

import java.util.ArrayList;
import java.util.List;

/**
 * 式の再帰下降パーサ
 * 
 * ## 文法
 * 
 * Expr -> LetExpr | AddSubExpr
 * 
 * LetExpr -> LET IDENT EQ Expr (IN Expr)?
 * 
 * AddSubExpr -> MulDivExpr ((ADDOP|SUBOP) MulDivExpr)?
 * MulDivExpr -> Primary ((MULOP|DIVOP) Primary)?
 * Primary -> LPAREN Expr RPAREN | INT | IDENT
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
        final var tok = toks.get(pos);
        // LetExpr
        if (tok.kind() == Token.Kind.LET) {
            return letExpr();
        }
        // AddSubExpr
        else {
            return addSubExpr();
        }
    }

    private Expr letExpr() {
        pos++; // consume LET
        final var ident = (Token.Ident) (toks.get(pos++));
        pos++; // consume EQ
        final var e = expr();
        var tok = toks.get(pos);
        // IN Expr
        if (tok.kind() == Token.Kind.IN) {
            pos++; // consume IN
            final var suc = expr();
            return new Expr.Let(ident.name, e, suc);
        }
        // (else)
        else {
            return new Expr.Let(ident.name, e);
        }
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
        // IDENT
        if (tok.kind() == Token.Kind.IDENT) {
            pos++; // consume IDENT
            final var ident = (Token.Ident) (tok);
            return new Expr.Ident(ident.name);
        }
        // error!
        throw new RuntimeException("unknown token: " + tok);
    }

}
