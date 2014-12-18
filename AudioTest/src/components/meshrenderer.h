#ifndef MESH_RENDERER_INCLUDED_H
#define MESH_RENDERER_INCLUDED_H

#include "entityComponent.h"

class MeshRenderer : public EntityComponent
{
public:
	MeshRenderer(IVertexArray* vertexArray, MaterialValues* material) :
		m_vertexArray(vertexArray)
	{
		m_uniformData.material = material;
	}

	virtual void Render(RenderParams& params)
	{
		m_uniformData.transform = GetTransform();
		m_uniformData.camera = params.camera;
		m_uniformData.renderData = params.renderValues;

		params.context->DrawVertexArray(params.target, params.shader, 
				m_vertexArray, m_uniformData);
	}
private:
	IVertexArray* m_vertexArray;
	UniformData m_uniformData;
};

#endif
