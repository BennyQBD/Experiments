;; This file is a manual, step-by-step compilation of a scheme program.
;; (compiled for the assembly language that is)
;; The purpose of this is to help visualize the compilation process, and
;; understand what needs to be done to generate code most effectively.

(define (fact n)
  (if (= n 1)
	1
	(* n (fact (- n 1)))))

;(define (f params) function)
;fact:;(n)

;(if cond val else)
;  cond
;  branch s0 val_label
;  else
;val_label:
;  val

;(= a b)
;  a
;  b
;  equals? s0 s1

;(* a b)
;  a
;  b
;  mul s0 s1

;(- a b)
;  a
;  b
;  sub s0 s1

;(+ a b)
;  a
;  b
;  add s0 s1


;(define (fact n) function)
;fact:;(n)

;(define (fact n) 
;  (if cond val else))
;fact:;(n)
;  cond
;  branch s0 val_label
;  else
;val_label:
;  val

;(define (fact n) 
;  (if (= n 1) val else))
;fact:;(n)
;  push s0
;  push 1
;  equals? s0 s1
;  branch s0 val_label
;  else
;val_label:
;  val

;(define (fact n) 
;  (if (= n 1) 1 else))
;fact:;(n)
;  push s0
;  push 1
;  equals? s0 s1
;  branch s0 val_label
;  else
;val_label:
;  push 1
;  ret s0

;(define (fact n) 
;  (if (= n 1) 1 (* n b)))
;fact:;(n)
;  push s0
;  push 1
;  equals? s0 s1
;  branch s0 val_label
;  push s3 ; n
;  b
;  mul n b
;val_label:
;  push 1
;  ret s0

;(define (fact n) 
;  (if (= n 1) 1 (* n (fact c))))
;fact:;(n)
;  push s0
;  push 1
;  equals? s0 s1
;  branch s0 val_label
;  push s3 ; n
;  c
;  fact c ; b
;  mul n b
;val_label:
;  push 1
;  ret s0

;----------------------------- COMPILATION 0 COMPLETE --------------------
;(define (fact n) 
;  (if (= n 1) 1 (* n (fact (- n 1)))))
;fact:;(n)
;  push s0
;  push 1
;  equals? s0 s1
;  branch val_label
;  push s3
;  push s4
;  push 1
;  sub s1 s0
;  fact s0
;  mul s8 s0
;  ret s0
;val_label:
;  push 1
;  ret s0

;----------------------------- COMPILATION OPTIMIZATION ------------------

; Include constants
;fact:;(n)
;  push s0
;  equals? s0 1
;  branch val_label
;  push s2
;  push s3
;  sub s0 1
;  fact s0
;  mul s6 s0
;  ret s0
;val_label:
;  ret 1

; Reuse known variables
;fact:;(n)
;  equals? s0 1
;  branch val_label
;  sub s1 1
;  fact s0
;  mul s3 s0
;  ret s0
;val_label:
;  ret 1

