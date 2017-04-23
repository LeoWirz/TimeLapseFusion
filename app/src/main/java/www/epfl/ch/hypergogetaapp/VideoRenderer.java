package www.epfl.ch.hypergogetaapp;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static java.lang.Math.min;

/**
 * Created by Williamallas on 26.03.2017.
 */

public class VideoRenderer implements GLSurfaceView.Renderer {

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        init();

        int n[] = new int[1];
        GLES30.glGetIntegerv(GLES30.GL_MAX_TEXTURE_IMAGE_UNITS, n,0);
        _maxTexUnit = n[0];
    }

    public void onDrawFrame(GL10 unused) {

        Bitmap toUpload = null;
        synchronized (_bitmaps) {
            toUpload = _bitmaps.poll();
        }

        if(toUpload != null){
            _glTextures.add(new int[1]);
            _glTextures.get(_glTextures.size()-1)[0] = uploadBitmap(toUpload);

            _bluredTextures.add(new int[1]);
            _bluredTextures.get(_bluredTextures.size()-1)[0] = createTexture(toUpload.getWidth(), toUpload.getHeight(), GLES30.GL_RGBA);
            if(_dummyTexture == null){
                _dummyTexture = new int[1];
                _dummyTexture[0] = createTexture(toUpload.getWidth(), toUpload.getHeight(), GLES30.GL_RGBA);
            }

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, _frameBuffer[0]);
            GLES30.glViewport(0, 0, toUpload.getWidth(), toUpload.getHeight());

            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, _dummyTexture[0], 0);
            render(_HBlurShader, _glTextures.get(_glTextures.size()-1)[0]);

            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, _bluredTextures.get(_bluredTextures.size()-1)[0], 0);
            render(_VBlurShader, _dummyTexture[0]);

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
            GLES30.glViewport(0, 0, _winWidth, _winHeight);
        }

        if(_needClear){
            for(int[] tex : _glTextures){
                GLES30.glDeleteTextures(1, tex, 0);
            }
            for(int[] tex : _bluredTextures){
                GLES30.glDeleteTextures(1, tex, 0);
            }
            _needClear = false;
            _glTextures.clear();
            _bluredTextures.clear();
        }

        render();
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        _winWidth = width;
        _winHeight = height;
        GLES30.glViewport(0, 0, width, height);
    }

    public void addFrame(Bitmap bitmap) {
        synchronized (_bitmaps) {
            _bitmaps.add(bitmap);
        }
    }

    public void clear() {
        synchronized (_bitmaps) {
            _bitmaps.clear();
            _needClear = true;
        }
    }

    public void setWindowSize(int size) {
        _windowSize = size;
    }

    private void init(){
        float[] uv = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f };

        float coef = 1.f;
        float[] position = {
                -1.0f*coef, 1.0f*coef,
                1.0f*coef, 1.0f*coef,
                1.0f*coef, -1.0f*coef,
                -1.0f*coef, -1.0f*coef };

        short[] indexes = { 0,1,2, 2,3,0 };

        _uvBuffer = createBuffer(uv);
        _vertexBuffer = createBuffer(position);
        _indexBuffer = createBuffer(indexes);

        _renderVideoShader = createShaderProgram(vertexShader_src, pixelShader_src);
        _HBlurShader = createShaderProgram(vertexShader_src, pixelHBlurShader_src);
        _VBlurShader = createShaderProgram(vertexShader_src, pixelVBlurShader_src);

        _nbTextureLocation = GLES30.glGetUniformLocation(_renderVideoShader, "nbTexture");

        GLES30.glGenFramebuffers(1, _frameBuffer, 0);

        GLES30.glDisable(GL10.GL_DEPTH_TEST);
        GLES30.glClearColor(0.6f, 0.6f, 0.6f, 1.0f);
    }

    private void render(){

        if(_glTextures.isEmpty()){
            //return;
        }
        else{
            GLES30.glUseProgram(_renderVideoShader);
            GLES30.glUniform1i(_nbTextureLocation, min(_glTextures.size(), _maxTexUnit / 2));

            int texs[] = new int[min(_glTextures.size(), _maxTexUnit / 2) * 2];
            for(int i=0 ; i<min(_glTextures.size(), _maxTexUnit / 2) ; ++i){
                texs[i*2] = _glTextures.get(i)[0];
                texs[i*2+1] = _bluredTextures.get(i)[0];
            }

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
            GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT);
            render(_renderVideoShader, texs);
        }

        GLES30.glUseProgram(0);
    }

    private void render(int shader, int texture) {
        int texs[] = new int[1];
        texs[0] = texture;
        render(shader, texs);
    }

    private void render(int shader, int textures[]) {
        GLES30.glUseProgram(shader);

        for(int i=0 ; i<textures.length ; ++i){
            GLES30.glActiveTexture(GLES20.GL_TEXTURE0 + i);
            GLES30.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);
        }

        /*************************/
        /* Setup attrib location */
        /*************************/

        int vertexAttribLocation = GLES30.glGetAttribLocation(_renderVideoShader, "vertex");
        int texCoordAttribLocation = GLES30.glGetAttribLocation(_renderVideoShader, "texCoord");

        // Enable generic vertex attribute array
        GLES30.glEnableVertexAttribArray(vertexAttribLocation);
        GLES30.glEnableVertexAttribArray(texCoordAttribLocation);

        // Prepare the quad coordinate data
        GLES30.glVertexAttribPointer(vertexAttribLocation, 2,
                GLES20.GL_FLOAT, false,
                0, _vertexBuffer);

        GLES30.glVertexAttribPointer(texCoordAttribLocation, 2,
                GLES20.GL_FLOAT, false,
                0, _uvBuffer);

        // Draw the quad
        GLES30.glDrawElements(GLES20.GL_TRIANGLES, 6,
                GLES20.GL_UNSIGNED_SHORT, _indexBuffer);

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(vertexAttribLocation);
        GLES30.glDisableVertexAttribArray(texCoordAttribLocation);

    }

    private int uploadBitmap(Bitmap bitmap){
        int[] tex = new int[1];
        GLES30.glGenTextures(1, tex, 0);

        GLES30.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES30.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);

        GLES30.glTexParameterf(GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_LINEAR);

        GLES30.glTexParameterf(GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_LINEAR);

        // upload the bitmap, no mipmap are created
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

        return tex[0];
    }

    // upload the data into buffer
    private FloatBuffer createBuffer(float[] data) {
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(data.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = byteBuf.asFloatBuffer();
        buffer.put(data);
        buffer.position(0);

        return buffer;
    }

    private ShortBuffer createBuffer(short[] data) {
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(data.length * 2);
        byteBuf.order(ByteOrder.nativeOrder());
        ShortBuffer buffer = byteBuf.asShortBuffer();
        buffer.put(data);
        buffer.position(0);

        return buffer;
    }

    private static int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES30.glCreateShader(type);

        GLES30.glShaderSource(shader, shaderCode);
        GLES30.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);	//compile[0] != 0 : compiled successfully
        if (compiled[0] == 0) {
            System.out.println("Error compiling shader:");
            System.out.println(shaderCode);
            System.out.println("----> Error:");
            System.out.println(GLES20.glGetShaderInfoLog(shader));
        }

        return shader;
    }

    private int createShaderProgram(String vShaderSrc, String pShaderSrc){
        int shader = GLES30.glCreateProgram();
        GLES30.glAttachShader(shader, loadShader(GLES30.GL_VERTEX_SHADER, vShaderSrc));   // add the vertex shader to program
        GLES30.glAttachShader(shader, loadShader(GLES30.GL_FRAGMENT_SHADER, pShaderSrc)); // add the fragment shader to program
        GLES30.glLinkProgram(shader);                                                     // creates OpenGL ES program executables

        GLES30.glUseProgram(shader);

        for(int i=0 ; i<_maxTexUnit ; ++i){
            int samplerLoc = GLES30.glGetUniformLocation (shader, "texture["+i+"]" );
            if(samplerLoc >= 0)
                GLES30.glUniform1i ( samplerLoc, i);
        }


        GLES30.glUseProgram(0);
        return shader;
    }

    private int createTexture(int w, int h, int internal){
        int id[] = new int[1];
        GLES30.glGenTextures(1, id, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, id[0]);
        // Width and height do not have to be a power of two
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA,
                w, h,
                0, internal, GLES30.GL_UNSIGNED_BYTE, null);

        GLES30.glTexParameterf(GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_LINEAR);

        GLES30.glTexParameterf(GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_LINEAR);

        return id[0];
    }

    private Queue<Bitmap> _bitmaps = new ArrayDeque<Bitmap>();
    private List<int[]> _glTextures = new ArrayList<int[]>();
    private List<int[]> _bluredTextures = new ArrayList<int[]>();
    private int[] _dummyTexture = null;
    private boolean _needClear = false;

    private int _windowSize = 1;
    private int _maxTexUnit = 16;

    private int _frameBuffer[] = new int[1];
    private FloatBuffer _vertexBuffer = null;
    private FloatBuffer _uvBuffer = null;
    private ShortBuffer _indexBuffer = null;
    private int _renderVideoShader = -1, _nbTextureLocation=-1;
    private int _HBlurShader = -1, _VBlurShader = -1;

    private int _winWidth=0, _winHeight=0;

    private static final String vertexShader_src =
            "attribute vec2 vertex; \n" +
            "attribute vec2 texCoord; \n" +
            "varying vec2 v_texCoord; \n" +
            "void main(){ \n" +
            "   v_texCoord = texCoord; \n" +
            "   gl_Position = vec4(vertex,0,1); \n" +
            "}";

    private static final String pixelShader_src =
            "precision highp float; \n" +
            "uniform sampler2D texture[32]; \n" +
            "uniform int nbTexture; \n" +
            "varying vec2 v_texCoord; \n" +
            "void main(){ \n" +
            "vec4 finalColor = vec4(0,0.1,0,0);\n" +
            "for(int i=0 ; i<nbTexture ; ++i){\n" +
            "   finalColor += texture2D( texture[i*2], v_texCoord );\n" +
            "}\n" +
            "   gl_FragColor = finalColor / (nbTexture > 0 ? float(nbTexture) : 1.0); \n" +
            "}";

    private static int KERNEL_SIZE = 9;
    private static final String pixelHBlurShader_src =
            "precision highp float; \n" +
            "uniform sampler2D texture[1]; \n" +
            "varying vec2 v_texCoord; \n" +
            "#define KERNEL_SIZE "+KERNEL_SIZE+"\n" +
            "const float STEP = 1.0/720.0;\n" +
            "void main(){ \n" +
            "   float KERNEL[KERNEL_SIZE];\n" +
            "   for(int i=0 ; i<KERNEL_SIZE ; ++i) KERNEL[i] = 1.0/float(KERNEL_SIZE);\n" +
            "   for(int i=0 ; i<KERNEL_SIZE ; ++i) gl_FragColor += KERNEL[i] * texture2D( texture[0], v_texCoord + vec2(float(i-((KERNEL_SIZE-1)/2)) * STEP,0) ); \n" +
            "}";

    private static final String pixelVBlurShader_src =
            "precision highp float; \n" +
            "uniform sampler2D texture[1]; \n" +
            "varying vec2 v_texCoord; \n" +
            "#define KERNEL_SIZE "+KERNEL_SIZE+"\n" +
            "const float STEP = 1.0/405.0;\n" +
            "void main(){ \n" +
            "   float KERNEL[KERNEL_SIZE];\n" +
            "   for(int i=0 ; i<KERNEL_SIZE ; ++i) KERNEL[i] = 1.0/float(KERNEL_SIZE);\n" +
            "   for(int i=0 ; i<KERNEL_SIZE ; ++i) gl_FragColor += KERNEL[i] * texture2D( texture[0], v_texCoord + vec2(0,float(i-((KERNEL_SIZE-1)/2)) * STEP) ); \n" +
            "}";
}