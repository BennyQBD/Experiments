require "./res/scripts/input"
require "./res/scripts/player"
require "./res/scripts/wall"
require "./res/scripts/ceiling"
require "./res/scripts/ball"
require "./res/scripts/bricks"

function GameAddEntity(ent)
	for _,e in pairs(Game_entities) do
		if e == ent then -- check if entity was already added
			return
		end
	end
    Game_entities[#Game_entities + 1] = ent
end

function GameInit()
	Game_entities = {}
	Game_ball = Ball(0.5, 0.5, 0.05, 0.05, 1,1,1)

	GameAddEntity(Player(0.4, 0.05, 0.8, 0.5, 0.5))
	GameAddEntity(Wall(0.0, 0, 0.05, 2, 0,0,1))
	GameAddEntity(Wall(1.95, 0, 0.05, 2, 0,0,1))
	GameAddEntity(Ceiling(0, 1.95, 2, 0.05, 0,0,1))
	GameAddEntity(Game_ball)

	for i = 1, 18 do
		for j = 1, 18 do
			GameAddEntity(Brick(i/10, j/20 + 0.8, 0.09, 
				0.04, 1,1,1))
		end
	end
	
	return 800, 600, "VM Game"
end

function GameUpdate(input, delta)
	for _,e in pairs(Game_entities) do
		e.Update(input, delta)
    end

	for _,e in pairs(Game_entities) do
		e.HandleCollision(Game_ball)
	end

end

function GameRender(context)
	RenderContext_Clear(context, 0.0, 0.0, 0.0, 0.0)
	for _,e in pairs(Game_entities) do
		e.Render(context)
	end
end
