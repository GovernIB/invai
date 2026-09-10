import{L as v,N as S,O as C,U as s}from"./chunk-Y66MR3DC.js";import{G as b}from"./chunk-QES7LLTG.js";import{i as k}from"./chunk-SV67CR7A.js";import{S as d,V as c,X as o,cb as p,cc as h,dc as g,gb as m,hb as u,lc as y,pb as f,ra as l}from"./chunk-PM3R7XGD.js";import{a as r,b as a}from"./chunk-TKJDIQEL.js";var I=`
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
/**i18n:85b369cd519586902b7f4f17a6e24b5cb5057fd4d248a08016454ba7f85e36fe*/
