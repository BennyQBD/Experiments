test:;(a b)
	add s0 s1
	ret s0

fact:;(n)
	equals? s0 1
	branch fact_one
	sub s1 1
	fact s0
	mul s0 s3
	ret s0 
fact_one:
	ret 1

main:;()
	fact 10
	ret s0
	

