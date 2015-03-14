  0  L10:   JUMP         L11
  1  L11:   LOADL        -1
  2         LOADL        2
  3         CALL         newobj  
  4         LOAD         0[LB]
  5         CALL         L10
  6         HALT   (0)   
