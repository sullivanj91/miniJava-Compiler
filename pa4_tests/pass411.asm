  0  L10:   JUMP         L11
  1  L11:   LOADL        -1
  2         LOADL        3
  3         CALL         newobj  
  4         LOAD         0[LB]
  5         LOADL        1
  6         LOADL        -1
  7         LOADL        2
  8         CALL         newobj  
  9         CALL         fieldupd
 10         LOAD         0[LB]
 11         LOADL        1
 12         CALL         fieldref
 13         LOADL        1
 14         LOAD         0[LB]
 15         CALL         fieldupd
 16         LOAD         0[LB]
 17         CALL         L10
 18         HALT   (0)   
