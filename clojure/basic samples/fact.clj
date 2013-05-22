(defn fact
	"Returns the factorial of n, which must be a positive integer."
	[n]
	(if (< n 2) 
		1 
		(* n (fact(dec n)))))