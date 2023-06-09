package toylang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
 * FnExpr -> VBAR (IDENT (COMMA IDENT)*)? VBAR ARROW Expr
 * 
 * AndOrExpr -> CompareExpr ((AND|OR) CompareExpr)?
 * CompareExpr -> AddSubExpr ((EQ|LT|GT) AddSubExpr)?
 * AddSubExpr -> MulDivExpr ((PLUS|MINUS) MulDivExpr)?
 * MulDivExpr -> unaryExpr ((STAR|SLASH) unaryExpr)?
 * UnaryExpr -> (NOT)? Apply
 * 
 * Apply -> Primary (ApplyParams)*
 * ApplyParams -> LPAREN (Expr (COMMA Expr)*)? RPAREN
 * 
 * Primary -> LPAREN Expr RPAREN
 *          | LPAREN RPAREN (=> Unit)
 *          | Apply
 *          | IDENT
 *          | INT
 *          | TRUE
 *          | FALSE
 */
public class Parser {
    // トークン列
    private final List<Token> toks;
    // 現在の入力位置
    private int pos;

    /**
     * コンストラクタ
     * @param lexer 字句解析器
     */
    public Parser(Lexer lexer) {
        this.toks = new ArrayList<>();
        this.pos = 0;

        Token tok = null;
        do {
            tok = lexer.next();
            this.toks.add(tok);
        } while (tok.kind() != Token.Kind.EOF);
    }

    /**
     * 入力が残っているかを判定する
     * @return 入力が残っていれば true, EOF（入力の終わり）に到達していたら false
     */
    public boolean hasNext() {
        return toks.get(pos).kind() != Token.Kind.EOF;
    }

    /**
     * 入力をパースして式のオブジェクトを求める
     * @return パース結果
     */
    public Expr parse() {
        return expr();
    }

    private Token consume(Token.Kind... exp) {
        assert exp.length > 0;

        final var tok = toks.get(pos++);
        if (!tok.kind().in(exp)) {
            final var expected = Arrays.asList(exp).stream() //
                                       .map(t -> t.toString()) //
                                       .collect(Collectors.joining(","));
            final var msg = String.format("expects token '%s', but found '%s'", expected, tok);
            throw new RuntimeException(msg);
        }
        return tok;
    }

    private Token.Kind lookahead() {
        return lookahead(0);
    }

    private Token.Kind lookahead(int k) {
        return toks.get(pos + k).kind();
    }

    private Expr.BinOp.Kind toBinOp(Token.Kind tokKind) {
        switch (tokKind) {
        case PLUS:
            return Expr.BinOp.Kind.ADD;
        case MINUS:
            return Expr.BinOp.Kind.SUB;
        case STAR:
            return Expr.BinOp.Kind.MUL;
        case SLASH:
            return Expr.BinOp.Kind.DIV;
        case EQ:
            return Expr.BinOp.Kind.EQ;
        case LT:
            return Expr.BinOp.Kind.LT;
        case GT:
            return Expr.BinOp.Kind.GT;
        case AND:
            return Expr.BinOp.Kind.AND;
        case OR:
            return Expr.BinOp.Kind.OR;
        default:
            throw new IllegalArgumentException("Unexpected value: " + tokKind);
        }
    }

    private Expr.UnaryOp.Kind toUnaryOp(Token.Kind tokKind) {
        switch (tokKind) {
        case NOT:
            return Expr.UnaryOp.Kind.NOT;
        case MINUS:
            return Expr.UnaryOp.Kind.MINUS;
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
        // FnExpr
        if (lookahead().in(Token.Kind.VBAR)) {
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

        consume(Token.Kind.VBAR);
        if (lookahead().in(Token.Kind.IDENT)) {
            var ident = (Token.Ident) consume(Token.Kind.IDENT);
            args.add(ident.name);
            while (lookahead().not(Token.Kind.VBAR)) {
                consume(Token.Kind.COMMA);
                ident = (Token.Ident) consume(Token.Kind.IDENT);
                args.add(ident.name);
            }
        }
        consume(Token.Kind.VBAR);
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
        if (lookahead().in(Token.Kind.AND, Token.Kind.OR)) {
            final var op = consume(Token.Kind.AND, Token.Kind.OR);
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
            final var op = consume(Token.Kind.EQ, Token.Kind.LT, Token.Kind.GT);
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
        if (lookahead().in(Token.Kind.PLUS, Token.Kind.MINUS)) {
            final var op = consume(Token.Kind.PLUS, Token.Kind.MINUS);
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
        if (lookahead().in(Token.Kind.STAR, Token.Kind.SLASH)) {
            final var op = consume(Token.Kind.STAR, Token.Kind.SLASH);
            final var r = unaryExpr();
            return new Expr.BinOp(toBinOp(op.kind()), l, r);
        }
        // Primary
        else {
            return l;
        }
    }

    private Expr unaryExpr() {
        // (NOT|MINUS) Expr
        if (lookahead().in(Token.Kind.NOT, Token.Kind.MINUS)) {
            final var op = consume(Token.Kind.NOT, Token.Kind.MINUS);
            final var e = apply();
            return new Expr.UnaryOp(toUnaryOp(op.kind()), e);
        }
        // Expr
        else {
            return apply();
        }
    }

    private Expr apply() {
        var e = primary();

        while (lookahead().in(Token.Kind.LPAREN)) {
            final var params = applyParams();
            // nullary (apply unit)
            if (params.isEmpty()) {
                e = new Expr.Apply(e, new Expr.Unit());
            }
            // 関数はカリー化しているので順に apply していく
            var app = new Expr.Apply(e, params.pop());
            while (!params.isEmpty()) {
                app = new Expr.Apply(app, params.pop());
            }
            e = app;
        }

        return e;
    }

    private LinkedList<Expr> applyParams() {
        final var params = new LinkedList<Expr>();

        consume(Token.Kind.LPAREN);
        if (lookahead().not(Token.Kind.RPAREN)) {
            Expr param = expr();
            params.add(param);
            while (lookahead().not(Token.Kind.RPAREN)) {
                consume(Token.Kind.COMMA);
                param = expr();
                params.add(param);

            }
        }
        consume(Token.Kind.RPAREN);

        return params;
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
