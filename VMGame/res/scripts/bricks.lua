require "./res/scripts/rectObject"

function Brick(x, y, width, height, r, g, b)
	local self = RectObject(x, y, width, height, r, g, b)

	self.isActive = 1

	function self.HandleCollision(other)
		if self.isActive == 1 then
			local isColliding = self.Intersect(other)

			if(isColliding ~= 0) then
				self.isActive = 0
				other.velY = other.velY * -1
			end
		end
	end

	function self.Render(context)
		if self.isActive ~= 0 then
			RenderContext_DrawSquare(context, self.x, self.y, 
				self.width, self.height, self.r, self.g, self.b)
		end
	end


	return self
end


