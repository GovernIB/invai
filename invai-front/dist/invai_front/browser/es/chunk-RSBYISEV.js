import{Q as M,S as N,T as B,Z as i}from"./chunk-OXT4V5UQ.js";import{F as I}from"./chunk-OWCIJYKA.js";import{Ha as c,Ia as h,Ja as k,K as l,Ka as v,M as d,N as s,R as f,aa as a,dc as D,ia as m,jb as S,mb as o,oa as g,sa as u,ta as y,xb as b,ya as p}from"./chunk-DMGFOOAC.js";var E=`
    .p-progressspinner {
        position: relative;
        margin: 0 auto;
        width: 100px;
        height: 100px;
        display: inline-block;
    }

    .p-progressspinner::before {
        content: '';
        display: block;
        padding-top: 100%;
    }

    .p-progressspinner-spin {
        height: 100%;
        transform-origin: center center;
        width: 100%;
        position: absolute;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
        margin: auto;
        animation: p-progressspinner-rotate 2s linear infinite;
    }

    .p-progressspinner-circle {
        stroke-dasharray: 89, 200;
        stroke-dashoffset: 0;
        stroke: dt('progressspinner.colorOne');
        animation:
            p-progressspinner-dash 1.5s ease-in-out infinite,
            p-progressspinner-color 6s ease-in-out infinite;
        stroke-linecap: round;
    }

    @keyframes p-progressspinner-rotate {
        100% {
            transform: rotate(360deg);
        }
    }
    @keyframes p-progressspinner-dash {
        0% {
            stroke-dasharray: 1, 200;
            stroke-dashoffset: 0;
        }
        50% {
            stroke-dasharray: 89, 200;
            stroke-dashoffset: -35px;
        }
        100% {
            stroke-dasharray: 89, 200;
            stroke-dashoffset: -124px;
        }
    }
    @keyframes p-progressspinner-color {
        100%,
        0% {
            stroke: dt('progressspinner.color.one');
        }
        40% {
            stroke: dt('progressspinner.color.two');
        }
        66% {
            stroke: dt('progressspinner.color.three');
        }
        80%,
        90% {
            stroke: dt('progressspinner.color.four');
        }
    }
`;var w={root:()=>["p-progressspinner"],spin:"p-progressspinner-spin",circle:"p-progressspinner-circle"},C=(()=>{class n extends M{name="progressspinner";style=E;classes=w;static \u0275fac=(()=>{let r;return function(e){return(r||(r=a(n)))(e||n)}})();static \u0275prov=l({token:n,factory:n.\u0275fac})}return n})();var F=new d("PROGRESSSPINNER_INSTANCE"),U=(()=>{class n extends B{componentName="ProgressSpinner";$pcProgressSpinner=s(F,{optional:!0,skipSelf:!0})??void 0;bindDirectiveInstance=s(i,{self:!0});styleClass;strokeWidth="2";fill="none";animationDuration="2s";ariaLabel;onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptms(["host","root"]))}_componentStyle=s(C);static \u0275fac=(()=>{let r;return function(e){return(r||(r=a(n)))(e||n)}})();static \u0275cmp=g({type:n,selectors:[["p-progressSpinner"],["p-progress-spinner"],["p-progressspinner"]],hostVars:5,hostBindings:function(t,e){t&2&&(p("aria-label",e.ariaLabel)("role","progressbar")("aria-busy",!0),o(e.cn(e.cx("root"),e.styleClass)))},inputs:{styleClass:"styleClass",strokeWidth:"strokeWidth",fill:"fill",animationDuration:"animationDuration",ariaLabel:"ariaLabel"},features:[b([C,{provide:F,useExisting:n},{provide:N,useExisting:n}]),u([i]),y],decls:2,vars:10,consts:[["viewBox","25 25 50 50",3,"pBind"],["cx","50","cy","50","r","20","stroke-miterlimit","10",3,"pBind"]],template:function(t,e){t&1&&(f(),h(0,"svg",0),v(1,"circle",1),k()),t&2&&(o(e.cx("spin")),S("animation-duration",e.animationDuration),c("pBind",e.ptm("spin")),m(),o(e.cx("circle")),c("pBind",e.ptm("circle")),p("fill",e.fill)("stroke-width",e.strokeWidth))},dependencies:[D,I,i],encapsulation:2,changeDetection:0})}return n})();export{U as a};
/**i18n:9fa542c6f17f570893e5bfce6175f9bb9e1d1bb7ee096369aaf6dc5812a678f2*/
