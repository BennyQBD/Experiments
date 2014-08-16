-- configuration file test
-- defines window size.
function f(x, y)
	width = 200
	height = 300
	background = {r=0.30, g=0.10, b=0}

	return (x^2 * math.sin(y))/(1 - x)
end

function testFunc(a, b)
	globalTest = MyTestFunction(a, b)--mySinFunction(b);
	return (a * b)/100.0
end
