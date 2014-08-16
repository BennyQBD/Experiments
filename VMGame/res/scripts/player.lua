require "./res/scripts/rectObject"

function Player(width, height, r, g, b)
	local self = RectObject(1 - (width/2), 0, width, height,
								r, g, b)

	function self.Update(input, delta)
		local moveAmt = delta

		if(Input_GetKey(input, KEY_RIGHT) ~= 0) then
			self.x = self.x + moveAmt
		end
		if(Input_GetKey(input, KEY_LEFT) ~= 0) then
			self.x = self.x - moveAmt
		end
	end

	function self.HandleCollision(other)
		local isColliding = self.Intersect(other)

		if(isColliding ~= 0) then
			self.isActive = 0
			other.velY = other.velY * -1
		end
	end

	return self
end

