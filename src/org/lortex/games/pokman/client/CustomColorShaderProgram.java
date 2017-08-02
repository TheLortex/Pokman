package org.lortex.games.pokman.client;

import java.util.Random;

import org.andengine.opengl.shader.PositionTextureCoordinatesShaderProgram;
import org.andengine.opengl.shader.ShaderProgram;
import org.andengine.opengl.shader.constants.ShaderProgramConstants;
import org.andengine.opengl.shader.exception.ShaderProgramException;
import org.andengine.opengl.shader.exception.ShaderProgramLinkException;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.attribute.VertexBufferObjectAttributes;

import android.opengl.GLES20;


public class CustomColorShaderProgram extends ShaderProgram {
			
	public final static String FRAGMENTSHADER = 
			"precision lowp float;\n" +

            "uniform sampler2D " + ShaderProgramConstants.UNIFORM_TEXTURE_0 + ";\n" +
            "varying mediump vec2 " + ShaderProgramConstants.VARYING_TEXTURECOORDINATES + ";\n" +
            
            "uniform vec4 theColor;\n" +

			"void main()	\n" +
			"{				\n" +
			"	vec4 tex = texture2D(" + ShaderProgramConstants.UNIFORM_TEXTURE_0 + ", vec2(" + ShaderProgramConstants.VARYING_TEXTURECOORDINATES + ".x, " + ShaderProgramConstants.VARYING_TEXTURECOORDINATES + ".y)); \n" +
			"	vec4 filter = tex * theColor; \n" +
			"	gl_FragColor = filter;	\n" +
			"}						\n";
	
	
	public CustomColorShaderProgram() {
		super(PositionTextureCoordinatesShaderProgram.VERTEXSHADER, FRAGMENTSHADER);
		
		r= new Random();
		r.setSeed(System.currentTimeMillis());
		do {
			mRed = r.nextFloat();
			mGreen = r.nextFloat();
			mBlue = r.nextFloat();
		}while(mRed<0.25f && mGreen<0.25f && mBlue<0.25f);
	}
	
	
	
    public static int sUniformModelViewPositionMatrixLocation = ShaderProgramConstants.LOCATION_INVALID;
    public static int sUniformTexture0Location = ShaderProgramConstants.LOCATION_INVALID;

    public static int colorLocation = ShaderProgramConstants.LOCATION_INVALID;
    
    private Random r ;
    
    public float mRed = 0.062f;
    public float mGreen = 0.062f;
    public float mBlue = 0.78f;
    
    
    @Override
    protected void link(final GLState pGLState) throws ShaderProgramLinkException {
        GLES20.glBindAttribLocation(this.mProgramID, ShaderProgramConstants.ATTRIBUTE_POSITION_LOCATION, ShaderProgramConstants.ATTRIBUTE_POSITION);
        GLES20.glBindAttribLocation(this.mProgramID, ShaderProgramConstants.ATTRIBUTE_TEXTURECOORDINATES_LOCATION, ShaderProgramConstants.ATTRIBUTE_TEXTURECOORDINATES);

        super.link(pGLState);

        CustomColorShaderProgram.sUniformModelViewPositionMatrixLocation = this.getUniformLocation(ShaderProgramConstants.UNIFORM_MODELVIEWPROJECTIONMATRIX);
        CustomColorShaderProgram.sUniformTexture0Location = this.getUniformLocation(ShaderProgramConstants.UNIFORM_TEXTURE_0);

        CustomColorShaderProgram.colorLocation = this.getUniformLocation("theColor");
        
    }
    
    @Override
    public void bind(final GLState pGLState, final VertexBufferObjectAttributes pVertexBufferObjectAttributes) {
        GLES20.glDisableVertexAttribArray(ShaderProgramConstants.ATTRIBUTE_COLOR_LOCATION);

        super.bind(pGLState, pVertexBufferObjectAttributes);
        
        GLES20.glUniformMatrix4fv(CustomColorShaderProgram.sUniformModelViewPositionMatrixLocation, 1, false, pGLState.getModelViewProjectionGLMatrix(), 0);
        GLES20.glUniform1i(CustomColorShaderProgram.sUniformTexture0Location, 0);

        GLES20.glUniform4f(colorLocation, mRed,mGreen, mBlue, 1f);
    }

  
    @Override
    public void unbind(final GLState pGLState) throws ShaderProgramException {
        GLES20.glEnableVertexAttribArray(ShaderProgramConstants.ATTRIBUTE_COLOR_LOCATION);

        super.unbind(pGLState);
    }
}