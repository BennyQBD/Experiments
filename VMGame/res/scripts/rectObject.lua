require "./res/scripts/entity"

function RectObject(x, y, width, height, r, g, b)
	local self = Entity()

	self.x = x
	self.y = y
	self.width = width
	self.height = height
	self.r = r
	self.g = g
	self.b = b
	
	function self.Render(context)
		RenderContext_DrawSquare(context, self.x, self.y, 
			self.width, self.height, self.r, self.g, self.b)
	end

	function self.Intersect(other)
		local minX1 = self.x 
		local minY1 = self.y 
		local maxX1 = self.x + self.width
		local maxY1 = self.y + self.height

		local minX2 = other.x 
		local minY2 = other.y 
		local maxX2 = other.x + other.width
		local maxY2 = other.y + other.height

		if (minX1 < maxX2 and maxX1 > minX2 and
		 minY1 < maxY2 and maxY1 > minY2) then
		 	return 1
		else
			return 0
		end

		--[[
		local distanceX1 = minX2 - minX1
		local distanceY1 = minY2 - minY1
		local distanceX2 = minX1 - minX2
		local distanceY2 = minY1 - minY2

		local distanceX = distanceX1
		local distanceY = distanceY1

		if distanceX2 > distanceX then
			distanceX = distanceX2
		end
		if distanceY2 > distanceY then
			distanceY = distanceY2
		end

		local maxDistance = distanceX

		if distanceY > maxDistance then
			maxDistance = distanceY
		end

		if maxDistance < 0 then
			return 1, distanceX, distanceY
		else
			return 0, distanceX, distanceY
		end
		--]]
		

	--Vector3f distances1 = other.GetMinExtents() - m_maxExtents;
	--Vector3f distances2 = m_minExtents - other.GetMaxExtents();
	--Vector3f distances = Vector3f(distances1.Max(distances2));
	--float maxDistance = distances.Max();
	--return IntersectData(maxDistance < 0, maxDistance);
	end

	return self
end

