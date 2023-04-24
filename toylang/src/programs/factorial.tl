// naive implementaion of factorial
let fact = |n| =>
	if n = 0 then 1
	else n * fact(n-1)
	
fact(0)
fact(1)
fact(5)
fact(10)