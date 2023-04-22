package toylang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 式の再帰下降パーサ
 * 
 * ## 文法
 * 
 * Expr -> LetExpr | IfExpr | FnExpr | AndOrExpr
 * 
 * LetExpr -> LET IDENT EQ Expr (IN Expr)?
 * 
 * IfExpr -> IF Expr THEN Expr (ELSE Expr)?
 * 
 * FnExpr -> FN LPAREN (IDENT (COMMA IDENT)*)? RPAREN ARROW Expr
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

    private Token consume(Token.Kind exp) {
        final var tok = toks.get(pos++);
        if (tok.kind() != exp) {
            final var msg = String.format("expects token '%s', but found '%s'", exp, tok);
            throw new RuntimeException(msg);
        }
        return tok;
    }

    private Token.Kind lookahead() {
        return toks.get(pos).kind();
    }

    private Expr.BinOp.Kind toBinOp(Token.Kind tokKind) {
        switch (tokKind) {
        case ADDOP:
            return Expr.BinOp.Kind.ADD;
        case SUBOP:
            return Expr.BinOp.Kind.SUB;
        case MULOP:
            return Expr.BinOp.Kind.MUL;
        case DIVOP:
            return Expr.BinOp.Kind.DIV;
        case EQ:
            return Expr.BinOp.Kind.EQ;
        case LT:
            return Expr.BinOp.Kind.LT;
        case GT:
            return Expr.BinOp.Kind.GT;
        case ANDAND:
            return Expr.BinOp.Kind.AND;
        case OROR:
            return Expr.BinOp.Kind.OR;
        default:
            throw new IllegalArgumentException("Unexpected value: " + tokKind);
        }
    }

    private Expr expr() {
        // LetExpr
        if (lookahead().in(Token.Kind.LET)) {
            return letExpr();
        }
        // IfExpr
        if (lookahead().in(Token.Kind.IF)) {
            return ifExpr();
        }
        if (lookahead().in(Token.Kind.FN)) {
            return fnExpr();
        }
        // AndOrExpr
        else {
            return andOrExpr();
        }
    }

    private Expr letExpr() {
        consume(Token.Kind.LET);
        final var ident = (Token.Ident) (consume(Token.Kind.IDENT));
        consume(Token.Kind.EQ);
        final var e = expr();
        // IN Expr
        if (lookahead().in(Token.Kind.IN)) {
            consume(Token.Kind.IN);
            final var suc = expr();
            return new Expr.Let(ident.name, e, suc);
        }
        // (else)
        else {
            return new Expr.Let(ident.name, e);
        }
    }

    private Expr ifExpr() {
        consume(Token.Kind.IF);
        final var cond = expr();
        consume(Token.Kind.THEN);
        final var then = expr();
        // ELSE Expr
        if (lookahead().in(Token.Kind.ELSE)) {
            consume(Token.Kind.ELSE);
            final var els = expr();
            return new Expr.If(cond, then, els);
        }
        // else
        else {
            return new Expr.If(cond, then, null);
        }
    }

    private Expr fnExpr() {
        final var args = new LinkedList<String>();

        consume(Token.Kind.FN);
        consume(Token.Kind.LPAREN);
        if (lookahead().in(Token.Kind.IDENT)) {
            var ident = (Token.Ident) consume(Token.Kind.IDENT);
            args.add(ident.name);
            while (lookahead().not(Token.Kind.RPAREN)) {
                consume(Token.Kind.COMMA);
                ident = (Token.Ident) consume(Token.Kind.IDENT);
                args.add(ident.name);
            }
        }
        consume(Token.Kind.RPAREN);
        consume(Token.Kind.ARROW);
        final var e = expr();

        // nullary
        if (args.size() == 0) {
            return new Expr.Func(null, e);
        }
        // more than 0 args - currying
        Collections.reverse(args);
        var fn = new Expr.Func(args.pop(), e);
        while (!args.isEmpty()) {
            fn = new Expr.Func(args.pop(), fn);
        }
        return fn;
    }

    private Expr andOrExpr() {
        final var l = compareExpr();
        // CompareExpr (ANDAND|OROR) CompareExpr
        if (lookahead().in(Token.Kind.ANDAND, Token.Kind.OROR)) {
            final var op = consume(Token.Kind.ANDAND);
            final var r = compareExpr();
            return new Expr.BinOp(toBinOp(op.kind()), l, r);
        }
        // CompareExpr
        else {
            return l;
        }
    }

    private Expr compareExpr() {
        final var l = addSubExpr();
        // AddSubExpr (EQ|LT|GT) AddSubExpr
        if (lookahead().in(Token.Kind.EQ, Token.Kind.LT, Token.Kind.GT)) {
            final var op = consume(Token.Kind.EQ);
            final var r = addSubExpr();
            return new Expr.BinOp(toBinOp(op.kind()), l, r);
        }
        // AddSubExpr
        else {
            return l;
        }
    }

    private Expr addSubExpr() {
        final var l = mulDivExpr();
        // MulDivExpr (ADDOP|SUBOP) MulDivExpr
        if (lookahead().in(Token.Kind.ADDOP, Token.Kind.SUBOP)) {
            final var op = consume(Token.Kind.ADDOP);
            final var r = mulDivExpr();
            return new Expr.BinOp(toBinOp(op.kind()), l, r);
        }
        // MulDivExpr
        else {
            return l;
        }
    }

    private Expr mulDivExpr() {
        final var l = unaryExpr();
        // Primary (MULOP|DIVOP) Primary
        if (lookahead().in(Token.Kind.MULOP, Token.Kind.DIVOP)) {
            final var op = consume(Token.Kind.MULOP);
            final var r = unaryExpr();
            return new Expr.BinOp(toBinOp(op.kind()), l, r);
        }
        // Primary
        else {
            return l;
        }
    }

    private Expr unaryExpr() {
        // NOT Expr
        if (lookahead().in(Token.Kind.NOT)) {
            consume(Token.Kind.NOT);
            final var e = primary();
            return new Expr.UnaryOp(Expr.UnaryOp.Kind.NOT, e);
        }
        // Expr
        else {
            return primary();
        }
    }

    private Expr primary() {
        // LPAREN
        if (lookahead().in(Token.Kind.LPAREN)) {
            consume(Token.Kind.LPAREN);
            Expr e = null;
            // Unit
            if (lookahead().in(Token.Kind.RPAREN)) {
                e = new Expr.Unit();
            }
            // Expr RPAREN
            else {
                e = expr();
            }
            consume(Token.Kind.RPAREN);
            return e;
        }
        // INT
        if (lookahead().in(Token.Kind.INT)) {
            final var lit = (Token.Int) (consume(Token.Kind.INT));
            return new Expr.Int(lit.n);
        }
        // IDENT
        if (lookahead().in(Token.Kind.IDENT)) {
            final var ident = (Token.Ident) (consume(Token.Kind.IDENT));
            return new Expr.Ident(ident.name);
        }
        // TRUE
        if (lookahead().in(Token.Kind.TRUE)) {
            consume(Token.Kind.TRUE);
            return new Expr.Bool(true);
        }
        // FALSE
        if (lookahead().in(Token.Kind.FALSE)) {
            consume(Token.Kind.FALSE);
            return new Expr.Bool(false);
        }
        // error!
        throw new RuntimeException("unknown token: " + lookahead());
    }

}
