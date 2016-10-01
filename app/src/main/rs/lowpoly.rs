#include "pragma.rsh"

#define STATUS_GRAY 1
#define STATUS_SOBEL 2

int status = 0;

const static float3 gMonoMult = {0.299f, 0.587f, 0.114f};

rs_script gScript;
rs_allocation gOriginal;
rs_allocation gGrayed;
rs_allocation gSobel;

int width,height;
int rand;
int accuracy = 10;

int2 points[15000];
int count =0;

static void setColor(int px,int py){
   float4 l = {1.0f,1.0f,1.0f,1.0f};
    rsSetElementAt(gSobel,&l, px, py);
}

void root(const uchar4 *v_in, uchar4 *v_out, uint32_t x, uint32_t y){
    if(status == STATUS_GRAY){
        float4 f4 = rsUnpackColor8888(*(v_in));
        float3 mono = dot(f4.rgb, gMonoMult);
        *v_out = rsPackColorTo8888(mono);
    }else if(status == STATUS_SOBEL){
        float4 lt = rsUnpackColor8888(*(v_in-width-1));
        float4 ct = rsUnpackColor8888(*(v_in-width));
        float4 rt = rsUnpackColor8888(*(v_in-width+1));

        float4 l = rsUnpackColor8888(*(v_in-1));
        float4 c = rsUnpackColor8888(*(v_in));
        float4 r = rsUnpackColor8888(*(v_in+1));

        float4 lb = rsUnpackColor8888(*(v_in+width-1));
        float4 cb = rsUnpackColor8888(*(v_in+width));
        float4 rb = rsUnpackColor8888(*(v_in+width+1));

        float gx = lt.x*(-1)+l.x*(-2)+lb.x*(-1)+
           rt.x*(1)+r.x*(2)+rb.x*(1);

        float gy = lt.x*(-1)+ct.x*(-2)+rt.x*(-1)+
           lb.x*(1)+cb.x*(2)+rb.x*(1);

        float G = sqrt(gx*gx+gy*gy);

        rand = rsRand(1.0f) * 10 * accuracy;
        if(G > 0.1f && rand == 1){
            setColor(x,y);
            int2 i2 = {x,y};
            points[count] = i2;
            count++;
        }else{
           float3 black = { 0.0f,0.0f,0.0f};
            *v_out = rsPackColorTo8888(black);
        }
    }
}

void process(int stat){
    status = stat;
    rsDebug("process==",status);
    if(status == STATUS_GRAY){
            rsForEach(gScript,gOriginal,gGrayed);
            rsDebug("process GRAY finish==",stat);
            rsSendToClient(101,&count,101);
    }else if(status == STATUS_SOBEL){
            count=0;
            rsForEach(gScript,gGrayed,gSobel);
            rsDebug("process SOBEL finish==",stat);
            rsSendToClient(102,&count,102);
    }
}

void send_points(){
    // to client
    int group = (count-1)/625+1;
    rsDebug("points group==",group);
    rsDebug("points size==",count);
    rsSendToClient(0,&count,group);

    for(int i=1;i<=group;i++){
        int index = 625 *(i-1);
        rsSendToClient(i,&(points[index]),4999);
    }
}