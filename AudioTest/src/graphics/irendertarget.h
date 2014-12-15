#ifndef I_RENDER_TARGET_INCLUDED_H
#define I_RENDER_TARGET_INCLUDED_H

class IRenderTarget
{
public:
	virtual ~IRenderTarget() {}
	virtual void Bind() = 0;
};

#endif
