require "./res/scripts/rectObject"

function Ceiling(x, y, width, height, r, g, b)
	local self = RectObject(x, y, width, height, r, g, b)
	
	function self.HandleCollision(other)
		local isColliding = self.Intersect(other)

		if(isColliding ~= 0) then
			self.isActive = 0
			other.velY = other.velY * -1
		end
	end

	return self
end

