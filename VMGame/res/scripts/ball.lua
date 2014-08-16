require "./res/scripts/rectObject"

function Ball(x, y, width, height, r, g, b)
	local self = RectObject(x, y, width, height, r, g, b)

	
	self.velX = 1
	self.velY = 1

	self.initialX = self.x
	self.initialY = self.y

	self.initialVelX = self.velX
	self.initialVelY = self.velY

	function self.Update(input, delta)
		local moveAmt = delta;

		self.x = self.x + self.velX * moveAmt
		self.y = self.y + self.velY * moveAmt

		if(self.y < 0) then
			self.x = self.initialX
			self.y = self.initialY
			self.velX = self.initialVelX
			self.velY = self.initialVelY
		end
	end

	return self
end


