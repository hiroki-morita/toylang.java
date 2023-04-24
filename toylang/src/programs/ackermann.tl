// アッカーマン関数
// see: https://ja.wikipedia.org/wiki/アッカーマン関数
let ackermann = |m, n| =>
	if m = 0 then n + 1
	else if n = 0 then ackermann(m-1, 1)
	else
		let m_prime = m - 1 in
		let n_prime = ackermann(m, n-1) in
		ackermann(m_prime, n_prime)
		
ackermann(0, 0)
ackermann(0, 1)
ackermann(0, 10)
// まだ動かない
//ackermann(3, 1)
//ackermann(3, 2)
//ackermann(3, 3)