package www.epfl.ch.hypergogetaapp;

import android.opengl.GLES20;
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

/**
 * Created by Williamallas on 26.03.2017.
 */

public class VideoRenderer implements GLSurfaceView.Renderer {

    public void init(){
        float[] uv = { 0.0f, 1.0f,
                       1.0f, 1.0f,
                       0.0f, 0.0f,
                       1.0f, 0.0f };

        float[] position = { -1.0f, 1.0f,
                             1.0f, 1.0f,
                             -1.0f, -1.0f,
                             1.0f, -1.0f };

        short[] indexes = { 0,1,2,3 };

        createBuffer(uv, _uvBuffer);
        createBuffer(position, _vertexBuffer);
        createBuffer(indexes, _indexBuffer);

        _renderVideoShader = createShaderProgram(vertexShader_src, pixelShader_src);

        GLES20.glDisable(GL10.GL_DEPTH_TEST);
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        //init();

        // Set the background frame color
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        System.out.println("OK init");
    }

    public void onDrawFrame(GL10 unused) {
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);

        /*Bitmap toUpload = null;
        synchronized (_bitmaps) {
            toUpload = _bitmaps.poll();
        }

        if(toUpload != null){
            _glTextures.add(new int[1]);
            _glTextures.get(_glTextures.size()-1)[0] = uploadBitmap(toUpload);
        }

        if(_needClear){
            for(int[] tex : _glTextures){
                GLES20.glDeleteTextures(1, tex, 0);
            }
            _needClear = false;
            _glTextures.clear();
        }

        render();*/
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
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

    public void setWindowSize(int size) {
        _windowSize = size;
    }

    private void render(){
        /*GLES20.glEnable(GL10.GL_TEXTURE_2D);
        GLES20.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
        GLES20.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        GLES20.glVert(2, GL10.GL_FLOAT, 0, textureBuffer);

// ... here goes the rendering of the mesh ...


        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisable(GL10.GL_TEXTURE_2D);*/

        if(_glTextures.isEmpty()){
            //return;
        }

        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        //GLES20.glUseProgram(_renderVideoShader);
    }

    private int uploadBitmap(Bitmap bitmap){
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);

        GLES20.glBindTexture(GL10.GL_TEXTURE_2D, tex[0]);

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

    // upload the data into buffer
    private void createBuffer(float[] data, FloatBuffer buffer) {
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(data.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        buffer = byteBuf.asFloatBuffer();
        buffer.put(data);
        buffer.position(0);
    }

    private void createBuffer(short[] data, ShortBuffer buffer) {
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(data.length * 2);
        byteBuf.order(ByteOrder.nativeOrder());
        buffer = byteBuf.asShortBuffer();
        buffer.put(data);
        buffer.position(0);
    }

    private static int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    private static int createShaderProgram(String vShaderSrc, String pShaderSrc){
        int shader = GLES20.glCreateProgram();
        GLES20.glAttachShader(shader, loadShader(GLES20.GL_VERTEX_SHADER, vShaderSrc));   // add the vertex shader to program
        GLES20.glAttachShader(shader, loadShader(GLES20.GL_FRAGMENT_SHADER, pShaderSrc)); // add the fragment shader to program
        GLES20.glLinkProgram(shader);                                                     // creates OpenGL ES program executables

        return shader;
    }

    private Queue<Bitmap> _bitmaps = new ArrayDeque<Bitmap>();
    private List<int[]> _glTextures = new ArrayList<int[]>();
    private boolean _needClear = false;

    private int _windowSize = 1;

    private FloatBuffer _vertexBuffer = null;
    private FloatBuffer _uvBuffer = null;
    private ShortBuffer _indexBuffer = null;
    private int _renderVideoShader = 0;

    private static final String vertexShader_src =
            "attribute vec4 vertex;" +
            "attribute vec2 texCoord;" +
            "varying vec2 v_texCoord;" +
            "void main(){" +
            "   v_texCoord = texCoord;" +
            "   gl_Position = vertex;" +
            "}";

    private static final String pixelShader_src =
            "precision mediump float;" +
            "uniform sampler2D texture[1];" +
            "varying vec2 v_texCoord;" +
            "void main(){" +
            "   gl_FragColor = texture2D( texture[0], v_texCoord );" +
            "}";
}