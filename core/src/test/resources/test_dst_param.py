# insert parameter a
def foo_a(a):
    print("Hello, One!")


# remove parameter b
def foo_b():
    print("Hello, Two!")


# update parameter c to d
def foo_c(d):
    print("Hello, Three!")


# update self to cls
def foo_d(cls):
    print("Hello, Four!");


# insert parameter e with default value
def foo_e(e="Alan"):
    print("Hello, Five!")


# remove parameter f with default value
def foo_f():
    print("Hello, Six!")


# update parameter g to h with the same default value
def foo_g(h=2021):
    print("Hello, Seven!")


# add parameter default value
def foo_h(i="Yuhe"):
    print("Hello, Eight!")


# remove parameter default value
def foo_i(j):
    print("Hello, Nine!")


# update parameter default value same data type
def foo_j(k="YYY"):
    print("Hello, Ten!")


# update parameter default value different data type
def foo_k(l="Sergio"):
    print("Hello, Eleven!")


# update parameter m to n, update default value
def foo_l(n="New"):
    print("Hello, Twelve!")


# insert parameter o, update default value of parameter p
def foo_m(o, p="Arnold"):
    print("Hello, Thirteen!")


# rename method, update parameter q to r
def bar_n(r):
    print("Hello, Fourteen!")


# rename method, update parameter default value
def bar_o(s="cats"):
    print("Hello, Fifteen!")
