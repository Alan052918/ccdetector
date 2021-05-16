# def foo_b(cls, c, d):
#     print("Method Relocation")
#
#
# def foo_a(self, a, b):
#     for i in range(1, 100):
#         output = ""
#         p = False
#         if i % 3 == 0:
#             p = True
#             output += "Fizz"
#         if i % 5 == 0:
#             p = True
#             output += "Buzz"
#         if p is True:
#             print(output)
#         else:
#             print(i)
#

def foo_c(self, e):
    for i in range(1, 10):
        msg = "No." + i
        if i % 2 == 0:
            msg += " Even number"
        else:
            msg += " Odd number"
        print(msg)