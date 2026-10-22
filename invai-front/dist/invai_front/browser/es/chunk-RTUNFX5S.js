import{T as v,V as S,W as C,aa as s}from"./chunk-A7IAJ6XT.js";import{G as b}from"./chunk-COF5ICWG.js";import{r as k}from"./chunk-SM2AXIHS.js";import{V as d,Y as c,_ as o,fb as p,fc as h,gc as g,jb as m,kb as u,oc as y,sb as f,ua as l}from"./chunk-26JRJ62F.js";import{a as r,b as a}from"./chunk-P6T6F6JQ.js";var I=`
    .p-skeleton {
        display: block;
        overflow: hidden;
        background: dt('skeleton.background');
        border-radius: dt('skeleton.border.radius');
    }

    .p-skeleton::after {
        content: '';
        animation: p-skeleton-animation 1.2s infinite;
        height: 100%;
        left: 0;
        position: absolute;
        right: 0;
        top: 0;
        transform: translateX(-100%);
        z-index: 1;
        background: linear-gradient(90deg, rgba(255, 255, 255, 0), dt('skeleton.animation.background'), rgba(255, 255, 255, 0));
    }

    [dir='rtl'] .p-skeleton::after {
        animation-name: p-skeleton-animation-rtl;
    }

    .p-skeleton-circle {
        border-radius: 50%;
    }

    .p-skeleton-animation-none::after {
        animation: none;
    }

    @keyframes p-skeleton-animation {
        from {
            transform: translateX(-100%);
        }
        to {
            transform: translateX(100%);
        }
    }

    @keyframes p-skeleton-animation-rtl {
        from {
            transform: translateX(100%);
        }
        to {
            transform: translateX(-100%);
        }
    }
`;var D={root:{position:"relative"}},N={root:({instance:e})=>["p-skeleton p-component",{"p-skeleton-circle":e.shape==="circle","p-skeleton-animation-none":e.animation==="none"}]},M=(()=>{class e extends v{name="skeleton";style=I;classes=N;inlineStyles=D;static \u0275fac=(()=>{let t;return function(n){return(t||(t=l(e)))(n||e)}})();static \u0275prov=d({token:e,factory:e.\u0275fac})}return e})();var w=new c("SKELETON_INSTANCE"),q=(()=>{class e extends C{componentName="Skeleton";$pcSkeleton=o(w,{optional:!0,skipSelf:!0})??void 0;bindDirectiveInstance=o(s,{self:!0});onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptms(["host","root"]))}styleClass;shape="rectangle";animation="wave";borderRadius;size;width="100%";height="1rem";_componentStyle=o(M);get containerStyle(){let t=this._componentStyle?.inlineStyles.root,i;return this.$unstyled()||(this.size?i=a(r({},t),{width:this.size,height:this.size,borderRadius:this.borderRadius}):i=a(r({},t),{width:this.width,height:this.height,borderRadius:this.borderRadius})),i}get dataP(){return this.cn({[this.shape]:this.shape})}static \u0275fac=(()=>{let t;return function(n){return(t||(t=l(e)))(n||e)}})();static \u0275cmp=p({type:e,selectors:[["p-skeleton"]],hostVars:6,hostBindings:function(i,n){i&2&&(f("aria-hidden",!0)("data-p",n.dataP),h(n.containerStyle),g(n.cn(n.cx("root"),n.styleClass)))},inputs:{styleClass:"styleClass",shape:"shape",animation:"animation",borderRadius:"borderRadius",size:"size",width:"width",height:"height"},features:[y([M,{provide:w,useExisting:e},{provide:S,useExisting:e}]),m([s]),u],decls:0,vars:0,template:function(i,n){},dependencies:[k,b],encapsulation:2,changeDetection:0})}return e})();export{q as a};
/**i18n:e8136fae3ab93b16771db13725b428f9aac2934b6934bfc6b4b028b903db9add*/
