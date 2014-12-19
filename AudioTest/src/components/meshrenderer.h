#ifndef MESH_RENDERER_INCLUDED_H
#define MESH_RENDERER_INCLUDED_H

#include "entityComponent.h"
#include "../graphics/mesh.h"
#include "../graphics/material.h"

class MeshRenderer : public EntityComponent
{
public:
	MeshRenderer(Mesh mesh, Material material) :
		m_mesh(mesh),
		m_material(material)
	{
		m_uniformData.material = material.GetValues();
	}

	virtual void Render(RenderParams& params)
	{
		m_uniformData.transform = GetTransform();
		m_uniformData.camera = params.camera;
		m_uniformData.renderData = params.renderValues;

		params.context->DrawVertexArray(params.target, params.shader, 
				m_mesh.GetVertexArray(), m_uniformData);
	}
private:
	Mesh m_mesh;
	Material m_material;
	UniformData m_uniformData;
};

#endif
