function Entity()
	local self = {}

	function self.Update(input, delta)
	end

	function self.HandleCollision(other)
	end

	function self.Render(context)
	end

	return self
end

--[[
Entities = {}
 
function addEntity(ent)
        for _,e in pairs(Entities) do
                if e == ent then -- check if entity was already added
                        return
                end
        end
        Entities[#Entities + 1] = ent
end
 
function updateEntities()
        for _,e in pairs(Entities) do
                e.Update()
        end
end
 
function renderEntities()
        for _,e in pairs(Entities) do
                e.Render()
        end
end
 
function Entity(entityName)
        local self = {}
        self.type = entityName
 
        -- declaring entity base functions
        function self.init()
 
        end
 
        return self
end
 
Player = Entity("Player")
 
function Player.init()
 
end
 
function Player.Update()
 
end
 
function Player.Render()
 
end
 
function Wall(x,y,width,height)
        local wall = Entity("Wall")
        wall.x = x
        wall.y = y
        wall.width = width
        wall.height = height
 
        function wall.init()
                -- init stuff
        end
 
        function wall.Update()
                -- update stuff
        end
 
        function wall.render()
                -- render stuff
        end    
 
        return wall
end
 
local wall = Wall(2, 2, 2, 2)
addEntity(wall)
 
local otherWall = Wall(1,1,1,1)
addEntity(wall)
--]]

--[[ 
function Entity()
        local self = {}
 
        -- declaring entity base functions
        function self.init()
 
        end
 
        return self
end
 
local player = Entity()
 
-- overriding methods :
 
function player.init()
 
end
--]]

