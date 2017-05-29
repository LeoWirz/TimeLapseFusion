package www.epfl.ch.hypergogetaapp;

import android.opengl.GLES20;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;

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
import static java.lang.Math.pow;

/**
 * Created by Williamallas on 26.03.2017.
 */

public class VideoRenderer implements GLSurfaceView.Renderer {

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        init();
    }

    public void onDrawFrame(GL10 unused) {
        /*GLES20.glClearColor(1,0,0,1);
        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT);
        return;*/

        Bitmap toUpload = null;
        synchronized (_bitmaps) {
            toUpload = _bitmaps.poll();
        }

        if(_needClear && toUpload != null){
            for(int[] tex : _glTextures){
                GLES20.glDeleteTextures(1, tex, 0);
            }
            for(int[] tex : _bluredTextures){
                GLES20.glDeleteTextures(1, tex, 0);
            }
            _needClear = false;
            _glTextures.clear();
            _bluredTextures.clear();
        }

        if(toUpload != null){
            _glTextures.add(new int[1]);
            _glTextures.get(_glTextures.size()-1)[0] = uploadBitmap(toUpload);

            _bluredTextures.add(new int[1]);
            _bluredTextures.get(_bluredTextures.size()-1)[0] = createTexture(toUpload.getWidth(), toUpload.getHeight(), GLES20.GL_RGBA);
            if(_dummyTexture == null){
                _dummyTexture = new int[1];
                _dummyTexture[0] = createTexture(toUpload.getWidth(), toUpload.getHeight(), GLES20.GL_RGBA);
            }

            checkGLError("Upload");

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, _frameBuffer[0]);
            GLES20.glViewport(0, 0, toUpload.getWidth(), toUpload.getHeight());

            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, _dummyTexture[0], 0);
            render(_HBlurShader, _glTextures.get(_glTextures.size()-1)[0]);

            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, _bluredTextures.get(_bluredTextures.size()-1)[0], 0);
            render(_VBlurShader, _dummyTexture[0]);

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GLES20.glViewport(0, 0, _winWidth, _winHeight);
        }

        if(_glTextures.isEmpty() || _windowSize == 0)
            return;

        // Render the video
        List<int[]> cappedColorTexture = _glTextures;
        List<int[]> cappedBluredTexture = _glTextures;
        if(_windowSize < _glTextures.size())
        {
            cappedColorTexture = cappedColorTexture.subList(_glTextures.size()-_windowSize, _glTextures.size());
            cappedBluredTexture = cappedBluredTexture.subList(_bluredTextures.size()-_windowSize, _bluredTextures.size());
        }

        int numIteration = (int)(cappedColorTexture.size() / (_maxTexUnit/2))
                + ((cappedColorTexture.size() % (_maxTexUnit/2) == 0) ? 0:1);

        int[] numTexturePerStep = new int[numIteration];

        for(int i=0 ; i<numIteration ; ++i)
        {
            int start = i * (_maxTexUnit/2);
            int end = min(start + (_maxTexUnit/2), cappedColorTexture.size());
            numTexturePerStep[i] = end - start;

            List<int[]> colorTex = new ArrayList<int[]>();
            List<int[]> bluredTex = new ArrayList<int[]>();
            for(int j=start ; j<end ; ++j)
            {
                colorTex.add(cappedColorTexture.get(j));
                bluredTex.add(cappedBluredTexture.get(j));
            }

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, _frameBuffer[0]);
            if(_intermediateTextures[i] < 0)
                _intermediateTextures[i] = createTexture(_winWidth, _winHeight, GLES20.GL_RGBA);

            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, _intermediateTextures[i], 0);
            render(colorTex, bluredTex);
        }

        // mix step
        float[] coefs = new float[_maxTexUnit];
        for(int i=numIteration-1 ; i>=0 ; --i) {
            coefs[i] = (i == numIteration - 1) ? 1.0f : coefs[i + 1] * (float) Math.pow(_sigma, ((float) numTexturePerStep[i + 1]));
        }


        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glUseProgram(_mixShader);
        GLES20.glUniform1i(_nbMixTextureLoc, numIteration);
        GLES20.glUniform1fv(_mixTextureCoefLoc, _maxTexUnit, coefs, 0);
        GLES20.glUniform1f(_brightnessLoc, _brightness);
        GLES20.glUniform1f(_contrastLoc, _contrast);
        render(_mixShader, _intermediateTextures, numIteration);

        gcTextures();
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        _winWidth = width;
        _winHeight = height;
        GLES20.glViewport(0, 0, width, height);
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

    public void setBrightness(float b) { _brightness = (b-0.5f) * 2;}
    public void setContrast(float c) { _contrast = c * 3; }
    public void setExpC(float x) { _expC = x; }
    public void setExpS(float x) { _expS = x; }
    public void setExpE(float x) { _expE = x; }
    public void setSigma(float x) { _sigma = x; }

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
        checkGLError("CreateBuffer");

        _renderVideoShader = createShaderProgram(vertexShader_src, pixelShader_src);
        _nbTextureLocation = GLES20.glGetUniformLocation(_renderVideoShader, "nbTexture");
        _expC_loc = GLES20.glGetUniformLocation(_renderVideoShader, "expC");
        _expS_loc = GLES20.glGetUniformLocation(_renderVideoShader, "expS");
        _expE_loc = GLES20.glGetUniformLocation(_renderVideoShader, "expE");
        _sigma_loc = GLES20.glGetUniformLocation(_renderVideoShader, "sigma");
        checkGLError("CreateShader1");

        _HBlurShader = createShaderProgram(vertexShader_src, pixelHBlurShader_src);
        checkGLError("CreateShader2");
        _VBlurShader = createShaderProgram(vertexShader_src, pixelVBlurShader_src);
        checkGLError("CreateShader3");

        _mixShader = createShaderProgram(vertexShader2_src, pixelMixStepShader_src);
        _nbMixTextureLoc = GLES20.glGetUniformLocation(_mixShader, "nbTexture");
        _mixTextureCoefLoc = GLES20.glGetUniformLocation(_mixShader, "coefTexture");
        _brightnessLoc = GLES20.glGetUniformLocation(_mixShader, "brightness");
        _contrastLoc = GLES20.glGetUniformLocation(_mixShader, "contrast");

        if(_expC_loc < 0 || _expS_loc < 0 || _expE_loc < 0 || _sigma_loc < 0 || _nbMixTextureLoc < 0 || _mixTextureCoefLoc < 0) {
            Log.d("Uniform exp? ", " not found");
            //System.exit(-1);
        }

        GLES20.glGenFramebuffers(1, _frameBuffer, 0);

        GLES20.glDisable(GL10.GL_DEPTH_TEST);
        GLES20.glClearColor(0.6f, 0.6f, 0.6f, 1.0f);

        // gen intermediate buffer
        for(int i=0 ; i<_maxTexUnit ; ++i)
            _intermediateTextures[i] = -1;
    }

    private void render(List<int[]> colorTex, List<int[]> bluredTex){
        if(colorTex.isEmpty() || colorTex.size() != bluredTex.size()){
            return;
        }
        else{
            int numTexToUse = min(colorTex.size(), _maxTexUnit / 2);

            if(numTexToUse == 0)
                return;

            GLES20.glUseProgram(_renderVideoShader);
            GLES20.glUniform1i(_nbTextureLocation, numTexToUse);
            GLES20.glUniform1f(_expC_loc, _expC);
            GLES20.glUniform1f(_expS_loc, _expS);
            GLES20.glUniform1f(_expE_loc, _expE);
            GLES20.glUniform1f(_sigma_loc, _sigma);

            int texs[] = new int[numTexToUse*2];

            for(int i=0 ; i<numTexToUse ; ++i){
                texs[i*2] = colorTex.get(i)[0];
                texs[i*2+1] = bluredTex.get(i)[0];
            }

            GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT);
            render(_renderVideoShader, texs, texs.length);
        }

        GLES20.glUseProgram(0);
    }

    private void render(int shader, int texture) {
        int texs[] = new int[1];
        texs[0] = texture;
        render(shader, texs, 1);
    }

    private void render(int shader, int textures[], int numTexture) {
        GLES20.glUseProgram(shader);

        for(int i=0 ; i<numTexture ; ++i) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);
        }

        /*************************/
        /* Setup attrib location */
        /*************************/

        int vertexAttribLocation = GLES20.glGetAttribLocation(_renderVideoShader, "vertex");
        int texCoordAttribLocation = GLES20.glGetAttribLocation(_renderVideoShader, "texCoord");

        // Enable generic vertex attribute array
        if(vertexAttribLocation >= 0)
            GLES20.glEnableVertexAttribArray(vertexAttribLocation);
        if(texCoordAttribLocation >= 0)
            GLES20.glEnableVertexAttribArray(texCoordAttribLocation);

        // Prepare the quad coordinate data
        if(vertexAttribLocation >= 0)
            GLES20.glVertexAttribPointer(vertexAttribLocation, 2,
                    GLES20.GL_FLOAT, false,
                    0, _vertexBuffer);

        if(texCoordAttribLocation >= 0)
            GLES20.glVertexAttribPointer(texCoordAttribLocation, 2,
                    GLES20.GL_FLOAT, false,
                    0, _uvBuffer);

        // Draw the quad
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6,
                GLES20.GL_UNSIGNED_SHORT, _indexBuffer);

        // Disable vertex array
        if(vertexAttribLocation >= 0)
            GLES20.glDisableVertexAttribArray(vertexAttribLocation);
        if(texCoordAttribLocation >= 0)
            GLES20.glDisableVertexAttribArray(texCoordAttribLocation);
    }

    private int uploadBitmap(Bitmap bitmap){
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);

        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_LINEAR);

        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_LINEAR);

        // upload the bitmap, no mipmap are created
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

        return tex[0];
    }

    private void gcTextures()
    {
        if(_glTextures.size() <= 100){
            return;
        }

        List<int[]> toRemove = _glTextures.subList(0, _glTextures.size()-100);
        List<int[]> toRemove2 = _bluredTextures.subList(0, _bluredTextures.size()-100);

        _glTextures = _glTextures.subList(_glTextures.size()-100, _glTextures.size());
        _bluredTextures = _bluredTextures.subList(_bluredTextures.size()-100, _bluredTextures.size());

        for(int[] tex : toRemove){
            GLES20.glDeleteTextures(1, tex, 0);
        }
        for(int[] tex : toRemove2){
            GLES20.glDeleteTextures(1, tex, 0);
        }
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
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);	//compile[0] != 0 : compiled successfully
        if (compiled[0] != GLES20.GL_TRUE) {
            Log.d("Error compiling shader:", GLES20.glGetShaderInfoLog(shader));
        }

        return shader;
    }

    private int createShaderProgram(String vShaderSrc, String pShaderSrc){
        int shader = GLES20.glCreateProgram();
        GLES20.glAttachShader(shader, loadShader(GLES20.GL_VERTEX_SHADER, vShaderSrc));   // add the vertex shader to program
        GLES20.glAttachShader(shader, loadShader(GLES20.GL_FRAGMENT_SHADER, pShaderSrc)); // add the fragment shader to program
        GLES20.glLinkProgram(shader);                                                     // creates OpenGL ES program executables

        int[] linked = new int[1];
        GLES20.glGetProgramiv(shader, GLES20.GL_LINK_STATUS, linked, 0);	//compile[0] != 0 : compiled successfully
        if (linked[0] != GLES20.GL_TRUE) {
            Log.d("Error linking shader:", GLES20.glGetProgramInfoLog(shader));
        }

        GLES20.glUseProgram(shader);

        for(int i=0 ; i<_maxTexUnit ; ++i){
            int samplerLoc = GLES20.glGetUniformLocation (shader, "texture["+i+"]" );
            if(samplerLoc >= 0)
                GLES20.glUniform1i ( samplerLoc, i);
        }


        GLES20.glUseProgram(0);
        return shader;
    }

    private int createTexture(int w, int h, int internal){
        int id[] = new int[1];
        GLES20.glGenTextures(1, id, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id[0]);
        // Width and height do not have to be a power of two
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                w, h,
                0, internal, GLES20.GL_UNSIGNED_BYTE, null);

        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_LINEAR);

        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_LINEAR);

        return id[0];
    }

    public void checkGLError(String op) {
        int error;
        int numError=0;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.d("GLError!! ", op + ": glError " + error);
            numError++;
        }
        if(numError > 0)
            System.exit(-1);
    }

    private Queue<Bitmap> _bitmaps = new ArrayDeque<Bitmap>();
    private List<int[]> _glTextures = new ArrayList<int[]>();
    private List<int[]> _bluredTextures = new ArrayList<int[]>();
    private int[] _dummyTexture = null;
    private boolean _needClear = false;

    private final int _maxTexUnit = 16;
    private int[] _intermediateTextures = new int[_maxTexUnit];

    private int _windowSize = 1;

    private int _frameBuffer[] = new int[1];
    private FloatBuffer _vertexBuffer = null;
    private FloatBuffer _uvBuffer = null;
    private ShortBuffer _indexBuffer = null;
    private int _renderVideoShader = -1, _nbTextureLocation=-1;
    private int _HBlurShader = -1, _VBlurShader = -1;
    private int _mixShader = -1, _nbMixTextureLoc = -1, _mixTextureCoefLoc = -1,
                _brightnessLoc = -1, _contrastLoc = -1;

    private float _brightness = 0.5f, _contrast = 0.5f;

    private float _expC  = 0.2f, _expS = 0.2f, _expE = 0.2f, _sigma = 0.7f;
    private int _expC_loc, _expS_loc, _expE_loc, _sigma_loc;

    private int _winWidth=0, _winHeight=0;

    private static final String vertexShader_src =
            "attribute vec2 vertex; \n" +
            "attribute vec2 texCoord; \n" +
            "varying vec2 v_texCoord; \n" +
            "void main(){ \n" +
            "   v_texCoord = texCoord; \n" +
            "   gl_Position = vec4(vertex,0,1); \n" +
            "}";

    private static final String vertexShader2_src =
            "attribute vec2 vertex; \n" +
            "attribute vec2 texCoord; \n" +
            "varying vec2 v_texCoord; \n" +
            "void main(){ \n" +
            "   v_texCoord = texCoord; \n" +
            "   gl_Position = vec4(vertex.x, -vertex.y, 0,1); \n" +
            "}";

    private static final String pixelShader_src =
            "precision highp float; \n" +
            "uniform sampler2D texture[16]; \n" +
            "uniform int nbTexture; \n" +
            "varying vec2 v_texCoord; \n" +

            "uniform float expC, expS, expE, sigma; \n" +

            "float toGrey(vec3 c){ return c.r*0.299+c.g*0.587+c.b*0.114; }\n" +

            "float exposdness(vec3 c){ return exp(-(c.r-0.5)*(c.r-0.5) / (2.0*0.2*0.2)) *\n" +
            "                                 exp(-(c.g-0.5)*(c.g-0.5) / (2.0*0.2*0.2)) *\n" +
            "                                 exp(-(c.b-0.5)*(c.b-0.5) / (2.0*0.2*0.2)); }\n" +

            "float saturation(vec3 c){ \n" +
            "   float mean = (c.r+c.g+c.b)/3.0;\n" +
            "   return sqrt((c.r-mean)*(c.r-mean) + (c.g-mean)*(c.g-mean) + (c.b-mean)*(c.b-mean)); " +
            "}\n" +

            "float timeDecay(int frame){\n" +
            "   //return exp(-float(frame*frame)*0.00175 / (2.0*0.2*0.2));\n" +
            "   return pow(sigma, float(frame));\n" +
            "}\n" +

            "void main(){ \n" +
            "if(nbTexture == 1){ \n" +
            "   gl_FragColor = texture2D( texture[0], v_texCoord ); return; }\n" +
            "vec4 finalColor = vec4(0,0,0,0);\n" +
            "float finalWeight = 0.0;\n" +
            "vec4 color=vec4(0,0,0,1);\n" +
            "for(int i=0 ; i<nbTexture ; ++i){\n" +
            "   color = texture2D( texture[i*2], v_texCoord );\n" +
            "   vec4 laplace = texture2D( texture[i*2+1], v_texCoord );\n" +
            "   float weight = pow(clamp(toGrey(color.rgb-laplace.rgb),0.0,1.0), expC);\n" +
            "   weight += pow(saturation(color.rgb), expS);\n" +
            "   weight += pow(exposdness(color.rgb), expE);\n" +
            "   //weight += 0.001;\n" +
            "   weight *= timeDecay(nbTexture-1-i);\n" +
            "   finalWeight += weight;\n" +
            "   finalColor += color*weight;\n" +
            "}\n" +
            "   gl_FragColor = finalColor / finalWeight; \n" +
            "}" +
            "\n" +
            "";

    private static int KERNEL_SIZE = 31;
    private static final String pixelHBlurShader_src =
            "precision highp float; \n" +
            "uniform sampler2D texture[1]; \n" +
            "varying vec2 v_texCoord; \n" +
            "#define KERNEL_SIZE "+KERNEL_SIZE+"\n" +
            "const float STEP = 1.0/480.0;\n" +
            "void main(){ \n" +
            "   float KERNEL[KERNEL_SIZE]; gl_FragColor = vec4(0,0,0,1);\n" +
            "   for(int i=0 ; i<KERNEL_SIZE ; ++i) KERNEL[i] = 1.0/float(KERNEL_SIZE);\n" +
            "   for(int i=0 ; i<KERNEL_SIZE ; ++i) gl_FragColor += KERNEL[i] * texture2D( texture[0], v_texCoord + vec2(float(i-((KERNEL_SIZE-1)/2)) * STEP,0) ); \n" +
            "}";

    private static final String pixelVBlurShader_src =
            "precision highp float; \n" +
            "uniform sampler2D texture[1]; \n" +
            "varying vec2 v_texCoord; \n" +
            "#define KERNEL_SIZE "+KERNEL_SIZE+"\n" +
            "const float STEP = 1.0/260.0;\n" +
            "void main(){ \n" +
            "   float KERNEL[KERNEL_SIZE]; gl_FragColor = vec4(0,0,0,1);\n" +
            "   for(int i=0 ; i<KERNEL_SIZE ; ++i) KERNEL[i] = 1.0/float(KERNEL_SIZE);\n" +
            "   for(int i=0 ; i<KERNEL_SIZE ; ++i) gl_FragColor += KERNEL[i] * texture2D( texture[0], v_texCoord + vec2(0,float(i-((KERNEL_SIZE-1)/2)) * STEP) ); \n" +
            "}";


    private static final String pixelMixStepShader_src =
            "precision highp float; \n" +
            "uniform sampler2D texture[16]; \n" +
            "varying vec2 v_texCoord; \n" +
            "uniform int nbTexture;\n" +
            "uniform float coefTexture[16];\n" +
            "uniform float brightness, contrast;\n" +
            "void main(){ \n" +
            "   vec3 finalColor = vec3(0,0,0);\n" +
            "   float sumCoef = 0.0;\n" +
            "   for(int i=0 ; i<nbTexture ; ++i){\n" +
            "       finalColor += coefTexture[i] * texture2D( texture[i], v_texCoord).xyz; \n" +
            "       sumCoef += coefTexture[i]; \n" +
            "   }\n" +
            "   gl_FragColor = vec4(finalColor / sumCoef, 1); \n" +
            "   gl_FragColor = (gl_FragColor - vec4(0.5,0.5,0.5,0.5)) * contrast + vec4(0.5,0.5,0.5,0.5);\n" +
            "   gl_FragColor += vec4(brightness,brightness,brightness,brightness);\n" +
            "}";
}