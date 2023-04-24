// true if n is even, otherwise false
let is_even = |n| =>
	if n = 0 then true
	else not is_even(n-1)
	
is_even(0)
is_even(1)
is_even(2)
is_even(31)
is_even(100)