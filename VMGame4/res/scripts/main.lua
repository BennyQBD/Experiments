function vec4(x, y, z, w)
	local self = {}
	if(x == nil) then
		x = 0
	end
	if(y == nil) then
		x = 0
	end
	if(z == nil) then
		x = 0
	end
	if(w == nil) then
		w = 0
	end

	self.x = x;
	self.y = y;
	self.z = z;
	self.w = w;

	function self.dot(b)
		return self.x * b.x + self.y * b.y + self.z + b.z + self.w * b.w
	end

	function self.cross(b)
		local x = self.y * b.z - self.z * b.y
		local y = self.z * b.x - self.x * b.z
		local z = self.x * b.y - self.y * b.x
		return vec4(x, y, z, w)
	end

	function self.length_squared()
		return self.dot(self)
	end

	function self.length()
		return math.sqrt(self.length_squared())
	end

	function self.normalized()
		local len = self.length()
		return vec4(self.x/len, self.y/len, self.z/len, self.w/len)
	end

	function self.lerp(dest, lerp_amt)
		return (dest - self) * lerp_amt + self
	end

	function self.reflect(normal)
		return self - (normal * (self.dot(normal) * 2))
	end
	
	function self.__add(a, b)
		return vec4(a.x + b.x, a.y + b.y, a.z + b.z, a.w + b.w)
	end

	function self.__sub(a, b)
		return vec4(a.x - b.x, a.y - b.y, a.z - b.z, a.w - b.w)
	end

	function self.__mul(a, b)
		return vec4(a.x * b, a.y * b, a.z * b, a.w * b)
	end

	function self.__unm(a)
		return vec4(-a.x, -a.y, -a.z, -a.w)
	end

	function self.__div(a, b)
		return self.__mul(a, 1.0/b)
	end

	function self.__eq(a, b)
		return a.x == b.x and a.y == b.y and a.z == b.z and a.w == b.w
	end
	
	function self.__tostring()
		return "(" .. self.x .. ", " .. self.y .. ", " ..
			self.z .. ", " .. self.w .. ")"
		end

	setmetatable(self, self)
	return self;
end

function Display2(width, height, title)
	local self = Display(width, height, title)

	function self.draw_rect(xStart, xEnd, yStart, yEnd, hex_color)
		for y = yStart, yEnd do
			for x = xStart, xEnd do
				self.draw_pixel(x, y, hex_color)
			end
		end
	end

	return self
end

function main()
	local v1 = vec4(1, 2, 3)
	local v2 = vec4(4, 5, 6)
	local v3 = v1.cross(v2)
	io.write("Hello, World: ")
	io.write(tostring(v3))
	io.write(tostring(v1 == v1))

	local display = Display2(800, 600, "My Display")

	while(display.is_closed() ~= true) do
		display.clear()
		display.draw_rect(100, 200, 100, 200, 0x00FF00)
		display.update()
	end
end
