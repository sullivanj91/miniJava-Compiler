  0         JUMP         L10
  1  L10:   LOADL        -1
  2         LOADL        5
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
 17         JUMP         L11
 18  L11:   CALL         L12
 19         HALT   (0)   
 20  L12:   LOADL        10
 21         LOADA        0[OB]
 22         LOADA        0[OB]
 23         LOADL        0
 24         CALL         fieldref
 25         LOADL        -49
 26         LOADL        4
 27         CALL         fieldupd
 28         LOADA        0[OB]
 29         LOADL        1
 30         CALL         fieldref
 31         LOAD         -49[LB]
 32         LOADL        0
 33         LOADL        5
 34         CALL         fieldupd
 35         LOADL        2
 36         LOADA        0[OB]
 37         LOAD         -51[LB]
 38         LOADA        0[OB]
 39         LOADA        0[OB]
 40         LOADL        1
 41         CALL         fieldref
 42         LOADL        -49
 43         CALL         fieldref
 44         LOADA        0[OB]
 45         LOAD         -51[LB]
 46         JUMP         L13
 47  L13:   CALL         L14
 48         CALL         add     
 49         CALL         putint  
 50         RETURN (0)   0
 51         LOADA        0[OB]
 52         LOADL        0
 53         CALL         fieldref
 54         LOAD         -1[LB]
 55         CALL         add     
 56         LOAD         -2[LB]
 57         CALL         add     
 58         RETURN (1)   2
 59  L14:   LOAD         -1[LB]
 60         LOADL        0
 61         CALL         fieldref
 62         LOAD         -2[LB]
 63         LOADL        0
 64         CALL         fieldref
 65         CALL         add     
 66         LOADA        0[OB]
 67         LOADA        0[OB]
 68         LOADL        0
 69         CALL         fieldref
 70         LOADL        -49
 71         CALL         fieldref
 72         CALL         add     
 73         RETURN (1)   2
 74         HALT   (0)   
