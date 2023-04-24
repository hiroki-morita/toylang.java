// fibonacci, naive implementation
let fib = |n| => 
	if n = 0 or n = 1 then n
	else fib(n-1) + fib(n-2)
	
fib(10)
fib(20)
fib(30)

// more faster implementation of fibonacci
let fib_faster = 
	let aux = |m, n, i| => 
		if i < 1 then m
		else aux(n, n + m, i - 1) in
	aux(0, 1)

fib_faster(10)
fib_faster(20)	
fib_faster(30)
