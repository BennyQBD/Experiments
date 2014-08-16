require "./res/scripts/input"

function GameInit()
	Game_X = 0.5
	Game_Y = 0.5

	return 800, 600, "VM Game"
end

function GameUpdate(input, delta)
	local moveAmt = delta

	if(Input_GetKey(input, KEY_RIGHT) ~= 0) then
		Game_X = Game_X + moveAmt
	end
	if(Input_GetKey(input, KEY_LEFT) ~= 0) then
		Game_X = Game_X - moveAmt
	end
	if(Input_GetKey(input, KEY_UP) ~= 0) then
		Game_Y = Game_Y + moveAmt
	end
	if(Input_GetKey(input, KEY_DOWN) ~= 0) then
		Game_Y = Game_Y - moveAmt
	end
end

function GameRender(context)
	RenderContext_Clear(context, 0.0, 0.0, 0.0, 0.0)
	RenderContext_DrawSquare(context, Game_X, Game_Y, 1, 1)
	
	--[[
	if Game_X > width or Game_X < 1 then
		renderX = false
		--RenderContext_Clear(context)
		Game_X_Inc = Game_X_Inc * -0.99
		X_Color = X_Color + 0x0307F0

		if Game_X > width then
			Game_X = width
		end

		if Game_X < 1 then
			Game_X = 1
		end
	end

	if(Game_Y > height or Game_Y < 1) then
		renderY = false
		--RenderContext_Clear(context)
		Game_Y_Inc = Game_Y_Inc * -0.99
		Y_Color = Y_Color + 0x0307F0

		if Game_Y > height then
			Game_Y = height
		end

		if Game_Y < 1 then
			Game_Y = 1
		end

	end
	
	if renderX then
		for j = 1, height do
			RenderContext_DrawPixel(context, Game_X, j, X_Color)
		end
	end

	if renderY then
		for i = 1, width do
			RenderContext_DrawPixel(context, i, Game_Y, Y_Color)
		end
	end
	--]]
end
