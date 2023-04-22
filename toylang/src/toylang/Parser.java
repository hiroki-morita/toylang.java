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
 * AndOrExpr -> CompareExpr ((ANDAND|OROR) CompareExpr)?
 * CompareExpr -> AddSubExpr ((EQ|LT|GT) AddSubExpr)?
 * AddSubExpr -> MulDivExpr ((ADDOP|SUBOP) MulDivExpr)?
 * MulDivExpr -> unaryExpr ((MULOP|DIVOP) unaryExpr)?
 * UnaryExpr -> (NOT)? Primary
 * 
 * Primary -> LPAREN Expr RPAREN
 *          | LPAREN RPAREN (=> Unit) 
 *          | INT
 *          | IDENT
 *          | TRUE
 *          | FALSE
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
        // AndOrExpr
        else {
            return andOrExpr();
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

    private Expr andOrExpr() {
        final var l = compareExpr();
        var tok = toks.get(pos);
        // CompareExpr ANDAND CompareExpr
        if (tok.kind() == Token.Kind.ANDAND) {
            pos++; // consume ANDAND
            final var r = compareExpr();
            return new Expr.BinOp(Expr.BinOp.Kind.AND, l, r);
        }
        // CompareExpr ANDAND CompareExpr
        if (tok.kind() == Token.Kind.OROR) {
            pos++; // consume OROR
            final var r = compareExpr();
            return new Expr.BinOp(Expr.BinOp.Kind.OR, l, r);
        }
        // CompareExpr
        else {
            return l;
        }
    }

    private Expr compareExpr() {
        final var l = addSubExpr();
        var tok = toks.get(pos);
        // AddSubExpr EQ AddSubExpr
        if (tok.kind() == Token.Kind.EQ) {
            pos++; // consume EQ
            final var r = addSubExpr();
            return new Expr.BinOp(Expr.BinOp.Kind.EQ, l, r);
        }
        // AddSubExpr LT AddSubExpr
        if (tok.kind() == Token.Kind.LT) {
            pos++; // consume LT
            final var r = addSubExpr();
            return new Expr.BinOp(Expr.BinOp.Kind.LT, l, r);
        }
        // AddSubExpr GT AddSubExpr
        if (tok.kind() == Token.Kind.GT) {
            pos++; // consume GT
            final var r = addSubExpr();
            return new Expr.BinOp(Expr.BinOp.Kind.GT, l, r);
        }
        // AddSubExpr
        else {
            return l;
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
        final var l = unaryExpr();
        var tok = toks.get(pos);
        // Primary MULOP Primary
        if (tok.kind() == Token.Kind.MULOP) {
            pos++; // consume MULOP
            final var r = unaryExpr();
            return new Expr.BinOp(Expr.BinOp.Kind.MUL, l, r);
        }
        // Primary DIVOP Primary
        if (tok.kind() == Token.Kind.DIVOP) {
            pos++; // consume DIVOP
            final var r = unaryExpr();
            return new Expr.BinOp(Expr.BinOp.Kind.DIV, l, r);
        }
        // Primary
        else {
            return l;
        }
    }

    private Expr unaryExpr() {
        var tok = toks.get(pos);
        // NOT Expr
        if (tok.kind() == Token.Kind.NOT) {
            pos++; // consume NOT
            final var e = primary();
            return new Expr.UnaryOp(Expr.UnaryOp.Kind.NOT, e);
        }
        // Expr
        else {
            return primary();
        }
    }

    private Expr primary() {
        var tok = toks.get(pos);
        // LPAREN
        if (tok.kind() == Token.Kind.LPAREN) {
            final var rParen = toks.get(pos + 1);
            // Unit
            if (rParen.kind() == Token.Kind.RPAREN) {
                pos += 2; // consume LPAREN RPAREN
                return new Expr.Unit();
            }
            // Expr RPAREN
            else {
                pos++; // consume LPAREN
                final var e = expr();
                pos++; // consume RPAREN
                return e;
            }
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
        // TRUE
        if (tok.kind() == Token.Kind.TRUE) {
            pos++; // consume TRUE
            return new Expr.Bool(true);
        }
        // FALSE
        if (tok.kind() == Token.Kind.FALSE) {
            pos++; // consume FALSE
            return new Expr.Bool(false);
        }
        // error!
        throw new RuntimeException("unknown token: " + tok);
    }

}
