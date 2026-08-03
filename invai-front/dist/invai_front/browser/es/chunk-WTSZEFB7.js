import{c as gt,e as _t,f as Oe,h as Fe}from"./chunk-VDXMXSPO.js";import{E as _e,G as We,J as ve,Q as dt,T as X,V as Y,W as J,_ as mt,aa as C,e as it,f as nt,fa as Be,ga as ae,ha as ut,k as ot,m as st,n as rt,o as lt,p as je,qa as ft,t as at,u as $e,y as ge}from"./chunk-VQATFZMM.js";import{B as pt,F as Te,G,H as ht,d as ct}from"./chunk-YLKOL3QT.js";import{e as Je,f as et,g as we,h as tt,i as Ie,l as le,n as De}from"./chunk-IXCWRJJ2.js";import{$ as T,$b as Ge,Ab as Ee,Bb as Pe,Cb as He,Db as te,Eb as H,Fb as j,Fc as h,Gb as ie,Hb as ne,Ib as Ue,La as Qe,Nc as A,Ob as B,Pa as d,Qb as r,R as Q,Rb as xe,S as pe,Sb as me,Tb as be,U as q,Ub as Ve,Vb as D,W as g,Wb as O,Ya as Se,_b as ue,aa as S,ba as N,bb as z,bc as se,cb as he,cc as w,db as oe,dc as Xe,ec as Ye,fb as K,ga as b,gb as _,ha as ke,hb as v,kc as U,la as Ce,lc as Le,mc as re,na as Ae,nc as ze,ob as L,qa as f,rb as qe,sb as Ke,wc as fe,xb as l,yb as V,zb as R,zc as $}from"./chunk-TI44TR7P.js";import{a as k,b as ye}from"./chunk-B4FWF4YO.js";var Ne=(()=>{class t extends J{modelValue=Ce(void 0);$filled=$(()=>ct(this.modelValue()));writeModelValue(e){this.modelValue.set(e)}static \u0275fac=(()=>{let e;return function(n){return(e||(e=f(t)))(n||t)}})();static \u0275dir=oe({type:t,features:[_]})}return t})();var vt=`
    .p-inputtext {
        font-family: inherit;
        font-feature-settings: inherit;
        font-size: 1rem;
        color: dt('inputtext.color');
        background: dt('inputtext.background');
        padding-block: dt('inputtext.padding.y');
        padding-inline: dt('inputtext.padding.x');
        border: 1px solid dt('inputtext.border.color');
        transition:
            background dt('inputtext.transition.duration'),
            color dt('inputtext.transition.duration'),
            border-color dt('inputtext.transition.duration'),
            outline-color dt('inputtext.transition.duration'),
            box-shadow dt('inputtext.transition.duration');
        appearance: none;
        border-radius: dt('inputtext.border.radius');
        outline-color: transparent;
        box-shadow: dt('inputtext.shadow');
    }

    .p-inputtext:enabled:hover {
        border-color: dt('inputtext.hover.border.color');
    }

    .p-inputtext:enabled:focus {
        border-color: dt('inputtext.focus.border.color');
        box-shadow: dt('inputtext.focus.ring.shadow');
        outline: dt('inputtext.focus.ring.width') dt('inputtext.focus.ring.style') dt('inputtext.focus.ring.color');
        outline-offset: dt('inputtext.focus.ring.offset');
    }

    .p-inputtext.p-invalid {
        border-color: dt('inputtext.invalid.border.color');
    }

    .p-inputtext.p-variant-filled {
        background: dt('inputtext.filled.background');
    }

    .p-inputtext.p-variant-filled:enabled:hover {
        background: dt('inputtext.filled.hover.background');
    }

    .p-inputtext.p-variant-filled:enabled:focus {
        background: dt('inputtext.filled.focus.background');
    }

    .p-inputtext:disabled {
        opacity: 1;
        background: dt('inputtext.disabled.background');
        color: dt('inputtext.disabled.color');
    }

    .p-inputtext::placeholder {
        color: dt('inputtext.placeholder.color');
    }

    .p-inputtext.p-invalid::placeholder {
        color: dt('inputtext.invalid.placeholder.color');
    }

    .p-inputtext-sm {
        font-size: dt('inputtext.sm.font.size');
        padding-block: dt('inputtext.sm.padding.y');
        padding-inline: dt('inputtext.sm.padding.x');
    }

    .p-inputtext-lg {
        font-size: dt('inputtext.lg.font.size');
        padding-block: dt('inputtext.lg.padding.y');
        padding-inline: dt('inputtext.lg.padding.x');
    }

    .p-inputtext-fluid {
        width: 100%;
    }
`;var At=`
    ${vt}

    /* For PrimeNG */
   .p-inputtext.ng-invalid.ng-dirty {
        border-color: dt('inputtext.invalid.border.color');
    }

    .p-inputtext.ng-invalid.ng-dirty::placeholder {
        color: dt('inputtext.invalid.placeholder.color');
    }
`,Pt={root:({instance:t})=>["p-inputtext p-component",{"p-filled":t.$filled(),"p-inputtext-sm":t.pSize==="small","p-inputtext-lg":t.pSize==="large","p-invalid":t.invalid(),"p-variant-filled":t.$variant()==="filled","p-inputtext-fluid":t.hasFluid}]},yt=(()=>{class t extends X{name="inputtext";style=At;classes=Pt;static \u0275fac=(()=>{let e;return function(n){return(e||(e=f(t)))(n||t)}})();static \u0275prov=Q({token:t,factory:t.\u0275fac})}return t})();var Ct=new q("INPUTTEXT_INSTANCE"),un=(()=>{class t extends Ne{componentName="InputText";hostName="";ptInputText=h();pInputTextPT=h();pInputTextUnstyled=h();bindDirectiveInstance=g(C,{self:!0});$pcInputText=g(Ct,{optional:!0,skipSelf:!0})??void 0;ngControl=g(ft,{optional:!0,self:!0});pcFluid=g(Be,{optional:!0,host:!0,skipSelf:!0});pSize;variant=h();fluid=h(void 0,{transform:A});invalid=h(void 0,{transform:A});$variant=$(()=>this.variant()||this.config.inputStyle()||this.config.inputVariant());_componentStyle=g(yt);constructor(){super(),Ae(()=>{let e=this.ptInputText()||this.pInputTextPT();e&&this.directivePT.set(e)}),Ae(()=>{this.pInputTextUnstyled()&&this.directiveUnstyled.set(this.pInputTextUnstyled())})}onAfterViewInit(){this.writeModelValue(this.ngControl?.value??this.el.nativeElement.value),this.cd.detectChanges()}onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptm("root"))}onDoCheck(){this.writeModelValue(this.ngControl?.value??this.el.nativeElement.value)}onInput(){this.writeModelValue(this.ngControl?.value??this.el.nativeElement.value)}get hasFluid(){return this.fluid()??!!this.pcFluid}get dataP(){return this.cn({invalid:this.invalid(),fluid:this.hasFluid,filled:this.$variant()==="filled",[this.pSize]:this.pSize})}static \u0275fac=function(i){return new(i||t)};static \u0275dir=oe({type:t,selectors:[["","pInputText",""]],hostVars:3,hostBindings:function(i,n){i&1&&B("input",function(){return n.onInput()}),i&2&&(L("data-p",n.dataP),w(n.cx("root")))},inputs:{hostName:"hostName",ptInputText:[1,"ptInputText"],pInputTextPT:[1,"pInputTextPT"],pInputTextUnstyled:[1,"pInputTextUnstyled"],pSize:"pSize",variant:[1,"variant"],fluid:[1,"fluid"],invalid:[1,"invalid"]},features:[U([yt,{provide:Ct,useExisting:t},{provide:Y,useExisting:t}]),K([C]),_]})}return t})(),fn=(()=>{class t{static \u0275fac=function(i){return new(i||t)};static \u0275mod=he({type:t});static \u0275inj=pe({})}return t})();var Ht=["data-p-icon","check"],vn=(()=>{class t extends ae{static \u0275fac=(()=>{let e;return function(n){return(e||(e=f(t)))(n||t)}})();static \u0275cmp=z({type:t,selectors:[["","data-p-icon","check"]],features:[_],attrs:Ht,decls:1,vars:0,consts:[["d","M4.86199 11.5948C4.78717 11.5923 4.71366 11.5745 4.64596 11.5426C4.57826 11.5107 4.51779 11.4652 4.46827 11.4091L0.753985 7.69483C0.683167 7.64891 0.623706 7.58751 0.580092 7.51525C0.536478 7.44299 0.509851 7.36177 0.502221 7.27771C0.49459 7.19366 0.506156 7.10897 0.536046 7.03004C0.565935 6.95111 0.613367 6.88 0.674759 6.82208C0.736151 6.76416 0.8099 6.72095 0.890436 6.69571C0.970973 6.67046 1.05619 6.66385 1.13966 6.67635C1.22313 6.68886 1.30266 6.72017 1.37226 6.76792C1.44186 6.81567 1.4997 6.8786 1.54141 6.95197L4.86199 10.2503L12.6397 2.49483C12.7444 2.42694 12.8689 2.39617 12.9932 2.40745C13.1174 2.41873 13.2343 2.47141 13.3251 2.55705C13.4159 2.64268 13.4753 2.75632 13.4938 2.87973C13.5123 3.00315 13.4888 3.1292 13.4271 3.23768L5.2557 11.4091C5.20618 11.4652 5.14571 11.5107 5.07801 11.5426C5.01031 11.5745 4.9368 11.5923 4.86199 11.5948Z","fill","currentColor"]],template:function(i,n){i&1&&(N(),te(0,"path",0))},encapsulation:2})}return t})();var jt=["data-p-icon","chevron-down"],xn=(()=>{class t extends ae{static \u0275fac=(()=>{let e;return function(n){return(e||(e=f(t)))(n||t)}})();static \u0275cmp=z({type:t,selectors:[["","data-p-icon","chevron-down"]],features:[_],attrs:jt,decls:1,vars:0,consts:[["d","M7.01744 10.398C6.91269 10.3985 6.8089 10.378 6.71215 10.3379C6.61541 10.2977 6.52766 10.2386 6.45405 10.1641L1.13907 4.84913C1.03306 4.69404 0.985221 4.5065 1.00399 4.31958C1.02276 4.13266 1.10693 3.95838 1.24166 3.82747C1.37639 3.69655 1.55301 3.61742 1.74039 3.60402C1.92777 3.59062 2.11386 3.64382 2.26584 3.75424L7.01744 8.47394L11.769 3.75424C11.9189 3.65709 12.097 3.61306 12.2748 3.62921C12.4527 3.64535 12.6199 3.72073 12.7498 3.84328C12.8797 3.96582 12.9647 4.12842 12.9912 4.30502C13.0177 4.48162 12.9841 4.662 12.8958 4.81724L7.58083 10.1322C7.50996 10.2125 7.42344 10.2775 7.32656 10.3232C7.22968 10.3689 7.12449 10.3944 7.01744 10.398Z","fill","currentColor"]],template:function(i,n){i&1&&(N(),te(0,"path",0))},encapsulation:2})}return t})();var xt=(()=>{class t extends Ne{required=h(void 0,{transform:A});invalid=h(void 0,{transform:A});disabled=h(void 0,{transform:A});name=h();_disabled=Ce(!1);$disabled=$(()=>this.disabled()||this._disabled());onModelChange=()=>{};onModelTouched=()=>{};writeDisabledState(e){this._disabled.set(e)}writeControlValue(e,i){}writeValue(e){this.writeControlValue(e,this.writeModelValue.bind(this))}registerOnChange(e){this.onModelChange=e}registerOnTouched(e){this.onModelTouched=e}setDisabledState(e){this.writeDisabledState(e),this.cd.markForCheck()}static \u0275fac=(()=>{let e;return function(n){return(e||(e=f(t)))(n||t)}})();static \u0275dir=oe({type:t,inputs:{required:[1,"required"],invalid:[1,"invalid"],disabled:[1,"disabled"],name:[1,"name"]},features:[_]})}return t})();var bt=["content"],$t=["overlay"],wt=["*","*"],Wt=()=>({mode:null}),Et=t=>({$implicit:t}),Zt=t=>({mode:t});function Qt(t,a){t&1&&ie(0)}function qt(t,a){if(t&1&&(me(0),v(1,Qt,1,0,"ng-container",3)),t&2){let e=r();d(),l("ngTemplateOutlet",e.contentTemplate||e._contentTemplate)("ngTemplateOutletContext",re(3,Et,Le(2,Wt)))}}function Kt(t,a){t&1&&ie(0)}function Ut(t,a){if(t&1){let e=ne();V(0,"div",5,0),B("click",function(){T(e);let n=r(2);return S(n.onOverlayClick())}),V(2,"p-motion",6),B("onBeforeEnter",function(n){T(e);let o=r(2);return S(o.onOverlayBeforeEnter(n))})("onEnter",function(n){T(e);let o=r(2);return S(o.onOverlayEnter(n))})("onAfterEnter",function(n){T(e);let o=r(2);return S(o.onOverlayAfterEnter(n))})("onBeforeLeave",function(n){T(e);let o=r(2);return S(o.onOverlayBeforeLeave(n))})("onLeave",function(n){T(e);let o=r(2);return S(o.onOverlayLeave(n))})("onAfterLeave",function(n){T(e);let o=r(2);return S(o.onOverlayAfterLeave(n))}),V(3,"div",5,1),B("click",function(n){T(e);let o=r(2);return S(o.onOverlayContentClick(n))}),me(5,1),v(6,Kt,1,0,"ng-container",3),R()()()}if(t&2){let e=r(2);se(e.sx("root")),w(e.cn(e.cx("root"),e.styleClass)),l("pBind",e.ptm("root")),d(2),l("visible",e.visible)("appear",!0)("options",e.computedMotionOptions()),d(),w(e.cn(e.cx("content"),e.contentStyleClass)),l("pBind",e.ptm("content")),d(3),l("ngTemplateOutlet",e.contentTemplate||e._contentTemplate)("ngTemplateOutletContext",re(15,Et,re(13,Zt,e.overlayMode)))}}function Gt(t,a){if(t&1&&v(0,Ut,7,17,"div",4),t&2){let e=r();l("ngIf",e.modalVisible)}}var Xt={root:()=>({position:"absolute",top:"0"})},Yt=`
.p-overlay-modal {
    display: flex;
    align-items: center;
    justify-content: center;
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
}

.p-overlay-content {
    transform-origin: inherit;
    will-change: transform;
}

/* Github Issue #18560 */
.p-component-overlay.p-component {
    position: relative;
}

.p-overlay-modal > .p-overlay-content {
    z-index: 1;
    width: 90%;
}

/* Position */
/* top */
.p-overlay-top {
    align-items: flex-start;
}
.p-overlay-top-start {
    align-items: flex-start;
    justify-content: flex-start;
}
.p-overlay-top-end {
    align-items: flex-start;
    justify-content: flex-end;
}

/* bottom */
.p-overlay-bottom {
    align-items: flex-end;
}
.p-overlay-bottom-start {
    align-items: flex-end;
    justify-content: flex-start;
}
.p-overlay-bottom-end {
    align-items: flex-end;
    justify-content: flex-end;
}

/* left */
.p-overlay-left {
    justify-content: flex-start;
}
.p-overlay-left-start {
    justify-content: flex-start;
    align-items: flex-start;
}
.p-overlay-left-end {
    justify-content: flex-start;
    align-items: flex-end;
}

/* right */
.p-overlay-right {
    justify-content: flex-end;
}
.p-overlay-right-start {
    justify-content: flex-end;
    align-items: flex-start;
}
.p-overlay-right-end {
    justify-content: flex-end;
    align-items: flex-end;
}

.p-overlay-content ~ .p-overlay-content {
    display: none;
}
`,Jt={host:"p-overlay-host",root:({instance:t})=>["p-overlay p-component",{"p-overlay-modal p-overlay-mask p-overlay-mask-enter-active":t.modal,"p-overlay-center":t.modal&&t.overlayResponsiveDirection==="center","p-overlay-top":t.modal&&t.overlayResponsiveDirection==="top","p-overlay-top-start":t.modal&&t.overlayResponsiveDirection==="top-start","p-overlay-top-end":t.modal&&t.overlayResponsiveDirection==="top-end","p-overlay-bottom":t.modal&&t.overlayResponsiveDirection==="bottom","p-overlay-bottom-start":t.modal&&t.overlayResponsiveDirection==="bottom-start","p-overlay-bottom-end":t.modal&&t.overlayResponsiveDirection==="bottom-end","p-overlay-left":t.modal&&t.overlayResponsiveDirection==="left","p-overlay-left-start":t.modal&&t.overlayResponsiveDirection==="left-start","p-overlay-left-end":t.modal&&t.overlayResponsiveDirection==="left-end","p-overlay-right":t.modal&&t.overlayResponsiveDirection==="right","p-overlay-right-start":t.modal&&t.overlayResponsiveDirection==="right-start","p-overlay-right-end":t.modal&&t.overlayResponsiveDirection==="right-end"}],content:"p-overlay-content"},It=(()=>{class t extends X{name="overlay";style=Yt;classes=Jt;inlineStyles=Xt;static \u0275fac=(()=>{let e;return function(n){return(e||(e=f(t)))(n||t)}})();static \u0275prov=Q({token:t,factory:t.\u0275fac})}return t})(),Tt=new q("OVERLAY_INSTANCE"),Un=(()=>{class t extends J{overlayService;zone;componentName="Overlay";$pcOverlay=g(Tt,{optional:!0,skipSelf:!0})??void 0;hostName="";get visible(){return this._visible}set visible(e){this._visible=e,this._visible&&!this.modalVisible&&(this.modalVisible=!0)}get mode(){return this._mode||this.overlayOptions?.mode}set mode(e){this._mode=e}get style(){return Oe.merge(this._style,this.modal?this.overlayResponsiveOptions?.style:this.overlayOptions?.style)}set style(e){this._style=e}get styleClass(){return Oe.merge(this._styleClass,this.modal?this.overlayResponsiveOptions?.styleClass:this.overlayOptions?.styleClass)}set styleClass(e){this._styleClass=e}get contentStyle(){return Oe.merge(this._contentStyle,this.modal?this.overlayResponsiveOptions?.contentStyle:this.overlayOptions?.contentStyle)}set contentStyle(e){this._contentStyle=e}get contentStyleClass(){return Oe.merge(this._contentStyleClass,this.modal?this.overlayResponsiveOptions?.contentStyleClass:this.overlayOptions?.contentStyleClass)}set contentStyleClass(e){this._contentStyleClass=e}get target(){let e=this._target||this.overlayOptions?.target;return e===void 0?"@prev":e}set target(e){this._target=e}get autoZIndex(){let e=this._autoZIndex||this.overlayOptions?.autoZIndex;return e===void 0?!0:e}set autoZIndex(e){this._autoZIndex=e}get baseZIndex(){let e=this._baseZIndex||this.overlayOptions?.baseZIndex;return e===void 0?0:e}set baseZIndex(e){this._baseZIndex=e}get showTransitionOptions(){let e=this._showTransitionOptions||this.overlayOptions?.showTransitionOptions;return e===void 0?".12s cubic-bezier(0, 0, 0.2, 1)":e}set showTransitionOptions(e){this._showTransitionOptions=e}get hideTransitionOptions(){let e=this._hideTransitionOptions||this.overlayOptions?.hideTransitionOptions;return e===void 0?".1s linear":e}set hideTransitionOptions(e){this._hideTransitionOptions=e}get listener(){return this._listener||this.overlayOptions?.listener}set listener(e){this._listener=e}get responsive(){return this._responsive||this.overlayOptions?.responsive}set responsive(e){this._responsive=e}get options(){return this._options}set options(e){this._options=e}appendTo=h(void 0);inline=h(!1);motionOptions=h(void 0);computedMotionOptions=$(()=>k(k({},this.ptm("motion")),this.motionOptions()||this.overlayOptions?.motionOptions));visibleChange=new b;onBeforeShow=new b;onShow=new b;onBeforeHide=new b;onHide=new b;onAnimationStart=new b;onAnimationDone=new b;onBeforeEnter=new b;onEnter=new b;onAfterEnter=new b;onBeforeLeave=new b;onLeave=new b;onAfterLeave=new b;overlayViewChild;contentViewChild;contentTemplate;templates;hostAttrSelector=h();$appendTo=$(()=>this.appendTo()||this.config.overlayAppendTo());_contentTemplate;_visible=!1;_mode;_style;_styleClass;_contentStyle;_contentStyleClass;_target;_autoZIndex;_baseZIndex;_showTransitionOptions;_hideTransitionOptions;_listener;_responsive;_options;modalVisible=!1;isOverlayClicked=!1;isOverlayContentClicked=!1;scrollHandler;documentClickListener;documentResizeListener;_componentStyle=g(It);bindDirectiveInstance=g(C,{self:!0});documentKeyboardListener;window;transformOptions={default:"scaleY(0.8)",center:"scale(0.7)",top:"translate3d(0px, -100%, 0px)","top-start":"translate3d(0px, -100%, 0px)","top-end":"translate3d(0px, -100%, 0px)",bottom:"translate3d(0px, 100%, 0px)","bottom-start":"translate3d(0px, 100%, 0px)","bottom-end":"translate3d(0px, 100%, 0px)",left:"translate3d(-100%, 0px, 0px)","left-start":"translate3d(-100%, 0px, 0px)","left-end":"translate3d(-100%, 0px, 0px)",right:"translate3d(100%, 0px, 0px)","right-start":"translate3d(100%, 0px, 0px)","right-end":"translate3d(100%, 0px, 0px)"};get modal(){if(De(this.platformId))return this.mode==="modal"||this.overlayResponsiveOptions&&this.document.defaultView?.matchMedia(this.overlayResponsiveOptions.media?.replace("@media","")||`(max-width: ${this.overlayResponsiveOptions.breakpoint})`).matches}get overlayMode(){return this.mode||(this.modal?"modal":"overlay")}get overlayOptions(){return k(k({},this.config?.overlayOptions),this.options)}get overlayResponsiveOptions(){return k(k({},this.overlayOptions?.responsive),this.responsive)}get overlayResponsiveDirection(){return this.overlayResponsiveOptions?.direction||"center"}get overlayEl(){return this.overlayViewChild?.nativeElement}get contentEl(){return this.contentViewChild?.nativeElement}get targetEl(){return lt(this.target,this.el?.nativeElement)}constructor(e,i){super(),this.overlayService=e,this.zone=i}onAfterContentInit(){this.templates?.forEach(e=>{e.getType()==="content"?this._contentTemplate=e.template:this._contentTemplate=e.template})}onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptm("host"))}show(e,i=!1){this.onVisibleChange(!0),this.handleEvents("onShow",{overlay:e||this.overlayEl,target:this.targetEl,mode:this.overlayMode}),i&&$e(this.targetEl),this.modal&&it(this.document?.body,"p-overflow-hidden")}hide(e,i=!1){if(this.visible)this.onVisibleChange(!1),this.handleEvents("onHide",{overlay:e||this.overlayEl,target:this.targetEl,mode:this.overlayMode}),i&&$e(this.targetEl),this.modal&&nt(this.document?.body,"p-overflow-hidden");else return}onVisibleChange(e){this._visible=e,this.visibleChange.emit(e)}onOverlayClick(){this.isOverlayClicked=!0}onOverlayContentClick(e){this.overlayService.add({originalEvent:e,target:this.targetEl}),this.isOverlayContentClicked=!0}container=Ce(void 0);onOverlayBeforeEnter(e){this.handleEvents("onBeforeShow",{overlay:this.overlayEl,target:this.targetEl,mode:this.overlayMode}),this.container.set(this.overlayEl||e.element),this.show(this.overlayEl,!0),this.hostAttrSelector()&&this.overlayEl&&this.overlayEl.setAttribute(this.hostAttrSelector(),""),this.appendOverlay(),this.alignOverlay(),this.setZIndex(),this.handleEvents("onBeforeEnter",e)}onOverlayEnter(e){this.handleEvents("onEnter",e)}onOverlayAfterEnter(e){this.bindListeners(),this.handleEvents("onAfterEnter",e)}onOverlayBeforeLeave(e){this.handleEvents("onBeforeHide",{overlay:this.overlayEl,target:this.targetEl,mode:this.overlayMode}),this.handleEvents("onBeforeLeave",e)}onOverlayLeave(e){this.handleEvents("onLeave",e)}onOverlayAfterLeave(e){this.hide(this.overlayEl,!0),this.container.set(null),this.unbindListeners(),this.appendOverlay(),Fe.clear(this.overlayEl),this.modalVisible=!1,this.cd.markForCheck(),this.handleEvents("onAfterLeave",e)}handleEvents(e,i){this[e].emit(i),this.options&&this.options[e]&&this.options[e](i),this.config?.overlayOptions&&(this.config?.overlayOptions)[e]&&(this.config?.overlayOptions)[e](i)}setZIndex(){this.autoZIndex&&Fe.set(this.overlayMode,this.overlayEl,this.baseZIndex+this.config?.zIndex[this.overlayMode])}appendOverlay(){this.$appendTo()&&this.$appendTo()!=="self"&&(this.$appendTo()==="body"?je(this.document.body,this.overlayEl):je(this.$appendTo(),this.overlayEl))}alignOverlay(){this.modal||this.overlayEl&&this.targetEl&&(this.overlayEl.style.minWidth=st(this.targetEl)+"px",this.$appendTo()==="self"?rt(this.overlayEl,this.targetEl):ot(this.overlayEl,this.targetEl))}bindListeners(){this.bindScrollListener(),this.bindDocumentClickListener(),this.bindDocumentResizeListener(),this.bindDocumentKeyboardListener()}unbindListeners(){this.unbindScrollListener(),this.unbindDocumentClickListener(),this.unbindDocumentResizeListener(),this.unbindDocumentKeyboardListener()}bindScrollListener(){this.scrollHandler||(this.scrollHandler=new mt(this.targetEl,e=>{(!this.listener||this.listener(e,{type:"scroll",mode:this.overlayMode,valid:!0}))&&this.hide(e,!0)})),this.scrollHandler.bindScrollListener()}unbindScrollListener(){this.scrollHandler&&this.scrollHandler.unbindScrollListener()}bindDocumentClickListener(){this.documentClickListener||(this.documentClickListener=this.renderer.listen(this.document,"click",e=>{let n=!(this.targetEl&&(this.targetEl.isSameNode(e.target)||!this.isOverlayClicked&&this.targetEl.contains(e.target)))&&!this.isOverlayContentClicked;(this.listener?this.listener(e,{type:"outside",mode:this.overlayMode,valid:e.which!==3&&n}):n)&&this.hide(e),this.isOverlayClicked=this.isOverlayContentClicked=!1}))}unbindDocumentClickListener(){this.documentClickListener&&(this.documentClickListener(),this.documentClickListener=null)}bindDocumentResizeListener(){this.documentResizeListener||(this.documentResizeListener=this.renderer.listen(this.document.defaultView,"resize",e=>{(this.listener?this.listener(e,{type:"resize",mode:this.overlayMode,valid:!ve()}):!ve())&&this.hide(e,!0)}))}unbindDocumentResizeListener(){this.documentResizeListener&&(this.documentResizeListener(),this.documentResizeListener=null)}bindDocumentKeyboardListener(){this.documentKeyboardListener||this.zone.runOutsideAngular(()=>{this.documentKeyboardListener=this.renderer.listen(this.document.defaultView,"keydown",e=>{if(this.overlayOptions.hideOnEscape===!1||e.code!=="Escape")return;(this.listener?this.listener(e,{type:"keydown",mode:this.overlayMode,valid:!ve()}):!ve())&&this.zone.run(()=>{this.hide(e,!0)})})})}unbindDocumentKeyboardListener(){this.documentKeyboardListener&&(this.documentKeyboardListener(),this.documentKeyboardListener=null)}onDestroy(){this.hide(this.overlayEl,!0),this.overlayEl&&this.$appendTo()!=="self"&&(this.renderer.appendChild(this.el.nativeElement,this.overlayEl),Fe.clear(this.overlayEl)),this.scrollHandler&&(this.scrollHandler.destroy(),this.scrollHandler=null),this.unbindListeners()}static \u0275fac=function(i){return new(i||t)(Se(pt),Se(ke))};static \u0275cmp=z({type:t,selectors:[["p-overlay"]],contentQueries:function(i,n,o){if(i&1&&be(o,bt,4)(o,Te,4),i&2){let s;D(s=O())&&(n.contentTemplate=s.first),D(s=O())&&(n.templates=s)}},viewQuery:function(i,n){if(i&1&&Ve($t,5)(bt,5),i&2){let o;D(o=O())&&(n.overlayViewChild=o.first),D(o=O())&&(n.contentViewChild=o.first)}},inputs:{hostName:"hostName",visible:"visible",mode:"mode",style:"style",styleClass:"styleClass",contentStyle:"contentStyle",contentStyleClass:"contentStyleClass",target:"target",autoZIndex:"autoZIndex",baseZIndex:"baseZIndex",showTransitionOptions:"showTransitionOptions",hideTransitionOptions:"hideTransitionOptions",listener:"listener",responsive:"responsive",options:"options",appendTo:[1,"appendTo"],inline:[1,"inline"],motionOptions:[1,"motionOptions"],hostAttrSelector:[1,"hostAttrSelector"]},outputs:{visibleChange:"visibleChange",onBeforeShow:"onBeforeShow",onShow:"onShow",onBeforeHide:"onBeforeHide",onHide:"onHide",onAnimationStart:"onAnimationStart",onAnimationDone:"onAnimationDone",onBeforeEnter:"onBeforeEnter",onEnter:"onEnter",onAfterEnter:"onAfterEnter",onBeforeLeave:"onBeforeLeave",onLeave:"onLeave",onAfterLeave:"onAfterLeave"},features:[U([It,{provide:Tt,useExisting:t},{provide:Y,useExisting:t}]),K([C]),_],ngContentSelectors:wt,decls:2,vars:1,consts:[["overlay",""],["content",""],[3,"class","style","pBind"],[4,"ngTemplateOutlet","ngTemplateOutletContext"],[3,"class","style","pBind","click",4,"ngIf"],[3,"click","pBind"],["name","p-anchored-overlay",3,"onBeforeEnter","onEnter","onAfterEnter","onBeforeLeave","onLeave","onAfterLeave","visible","appear","options"]],template:function(i,n){i&1&&(xe(wt),qe(0,qt,2,5)(1,Gt,1,1,"div",2)),i&2&&Ke(n.inline()?0:1)},dependencies:[le,we,Ie,G,C,_t,gt],encapsulation:2,changeDetection:0})}return t})();var ei=["data-p-icon","angle-right"],Jn=(()=>{class t extends ae{static \u0275fac=(()=>{let e;return function(n){return(e||(e=f(t)))(n||t)}})();static \u0275cmp=z({type:t,selectors:[["","data-p-icon","angle-right"]],features:[_],attrs:ei,decls:1,vars:0,consts:[["d","M5.25 11.1728C5.14929 11.1694 5.05033 11.1455 4.9592 11.1025C4.86806 11.0595 4.78666 10.9984 4.72 10.9228C4.57955 10.7822 4.50066 10.5916 4.50066 10.3928C4.50066 10.1941 4.57955 10.0035 4.72 9.86283L7.72 6.86283L4.72 3.86283C4.66067 3.71882 4.64765 3.55991 4.68275 3.40816C4.71785 3.25642 4.79932 3.11936 4.91585 3.01602C5.03238 2.91268 5.17819 2.84819 5.33305 2.83149C5.4879 2.81479 5.64411 2.84671 5.78 2.92283L9.28 6.42283C9.42045 6.56346 9.49934 6.75408 9.49934 6.95283C9.49934 7.15158 9.42045 7.34221 9.28 7.48283L5.78 10.9228C5.71333 10.9984 5.63193 11.0595 5.5408 11.1025C5.44966 11.1455 5.35071 11.1694 5.25 11.1728Z","fill","currentColor"]],template:function(i,n){i&1&&(N(),te(0,"path",0))},encapsulation:2})}return t})();var ti=["data-p-icon","times-circle"],zt=(()=>{class t extends ae{pathId;onInit(){this.pathId="url(#"+dt()+")"}static \u0275fac=(()=>{let e;return function(n){return(e||(e=f(t)))(n||t)}})();static \u0275cmp=z({type:t,selectors:[["","data-p-icon","times-circle"]],features:[_],attrs:ti,decls:5,vars:2,consts:[["fill-rule","evenodd","clip-rule","evenodd","d","M7 14C5.61553 14 4.26215 13.5895 3.11101 12.8203C1.95987 12.0511 1.06266 10.9579 0.532846 9.67879C0.00303296 8.3997 -0.13559 6.99224 0.134506 5.63437C0.404603 4.2765 1.07129 3.02922 2.05026 2.05026C3.02922 1.07129 4.2765 0.404603 5.63437 0.134506C6.99224 -0.13559 8.3997 0.00303296 9.67879 0.532846C10.9579 1.06266 12.0511 1.95987 12.8203 3.11101C13.5895 4.26215 14 5.61553 14 7C14 8.85652 13.2625 10.637 11.9497 11.9497C10.637 13.2625 8.85652 14 7 14ZM7 1.16667C5.84628 1.16667 4.71846 1.50879 3.75918 2.14976C2.79989 2.79074 2.05222 3.70178 1.61071 4.76768C1.16919 5.83358 1.05367 7.00647 1.27876 8.13803C1.50384 9.26958 2.05941 10.309 2.87521 11.1248C3.69102 11.9406 4.73042 12.4962 5.86198 12.7212C6.99353 12.9463 8.16642 12.8308 9.23232 12.3893C10.2982 11.9478 11.2093 11.2001 11.8502 10.2408C12.4912 9.28154 12.8333 8.15373 12.8333 7C12.8333 5.45291 12.2188 3.96918 11.1248 2.87521C10.0308 1.78125 8.5471 1.16667 7 1.16667ZM4.66662 9.91668C4.58998 9.91704 4.51404 9.90209 4.44325 9.87271C4.37246 9.84333 4.30826 9.8001 4.2544 9.74557C4.14516 9.6362 4.0838 9.48793 4.0838 9.33335C4.0838 9.17876 4.14516 9.0305 4.2544 8.92113L6.17553 7L4.25443 5.07891C4.15139 4.96832 4.09529 4.82207 4.09796 4.67094C4.10063 4.51982 4.16185 4.37563 4.26872 4.26876C4.3756 4.16188 4.51979 4.10066 4.67091 4.09799C4.82204 4.09532 4.96829 4.15142 5.07887 4.25446L6.99997 6.17556L8.92106 4.25446C9.03164 4.15142 9.1779 4.09532 9.32903 4.09799C9.48015 4.10066 9.62434 4.16188 9.73121 4.26876C9.83809 4.37563 9.89931 4.51982 9.90198 4.67094C9.90464 4.82207 9.84855 4.96832 9.74551 5.07891L7.82441 7L9.74554 8.92113C9.85478 9.0305 9.91614 9.17876 9.91614 9.33335C9.91614 9.48793 9.85478 9.6362 9.74554 9.74557C9.69168 9.8001 9.62748 9.84333 9.55669 9.87271C9.4859 9.90209 9.40996 9.91704 9.33332 9.91668C9.25668 9.91704 9.18073 9.90209 9.10995 9.87271C9.03916 9.84333 8.97495 9.8001 8.9211 9.74557L6.99997 7.82444L5.07884 9.74557C5.02499 9.8001 4.96078 9.84333 4.88999 9.87271C4.81921 9.90209 4.74326 9.91704 4.66662 9.91668Z","fill","currentColor"],[3,"id"],["width","14","height","14","fill","white"]],template:function(i,n){i&1&&(N(),Pe(0,"g"),te(1,"path",0),He(),Pe(2,"defs")(3,"clipPath",1),te(4,"rect",2),He()()),i&2&&(L("clip-path",n.pathId),d(3),Ue("id",n.pathId))},encapsulation:2})}return t})();var Dt=["content"],ii=["item"],ni=["loader"],oi=["loadericon"],si=["element"],ri=["*"],Ze=(t,a)=>({$implicit:t,options:a}),li=t=>({numCols:t}),kt=t=>({options:t}),ai=()=>({styleClass:"p-virtualscroller-loading-icon"}),ci=(t,a)=>({rows:t,columns:a});function di(t,a){t&1&&ie(0)}function pi(t,a){if(t&1&&(H(0),v(1,di,1,0,"ng-container",10),j()),t&2){let e=r(2);d(),l("ngTemplateOutlet",e.contentTemplate||e._contentTemplate)("ngTemplateOutletContext",ze(2,Ze,e.loadedItems,e.getContentOptions()))}}function hi(t,a){t&1&&ie(0)}function mi(t,a){if(t&1&&(H(0),v(1,hi,1,0,"ng-container",10),j()),t&2){let e=a.$implicit,i=a.index,n=r(3);d(),l("ngTemplateOutlet",n.itemTemplate||n._itemTemplate)("ngTemplateOutletContext",ze(2,Ze,e,n.getOptions(i)))}}function ui(t,a){if(t&1&&(V(0,"div",11,3),v(2,mi,2,5,"ng-container",12),R()),t&2){let e=r(2);se(e.contentStyle),w(e.cn(e.cx("content"),e.contentStyleClass)),l("pBind",e.ptm("content")),d(2),l("ngForOf",e.loadedItems)("ngForTrackBy",e._trackBy)}}function fi(t,a){if(t&1&&Ee(0,"div",13),t&2){let e=r(2);w(e.cx("spacer")),l("ngStyle",e.spacerStyle)("pBind",e.ptm("spacer"))}}function gi(t,a){t&1&&ie(0)}function _i(t,a){if(t&1&&(H(0),v(1,gi,1,0,"ng-container",10),j()),t&2){let e=a.index,i=r(4);d(),l("ngTemplateOutlet",i.loaderTemplate||i._loaderTemplate)("ngTemplateOutletContext",re(4,kt,i.getLoaderOptions(e,i.both&&re(2,li,i.numItemsInViewport.cols))))}}function vi(t,a){if(t&1&&(H(0),v(1,_i,2,6,"ng-container",14),j()),t&2){let e=r(3);d(),l("ngForOf",e.loaderArr)}}function yi(t,a){t&1&&ie(0)}function Ci(t,a){if(t&1&&(H(0),v(1,yi,1,0,"ng-container",10),j()),t&2){let e=r(4);d(),l("ngTemplateOutlet",e.loaderIconTemplate||e._loaderIconTemplate)("ngTemplateOutletContext",re(3,kt,Le(2,ai)))}}function xi(t,a){if(t&1&&(N(),Ee(0,"svg",15)),t&2){let e=r(4);w(e.cx("loadingIcon")),l("spin",!0)("pBind",e.ptm("loadingIcon"))}}function bi(t,a){if(t&1&&v(0,Ci,2,5,"ng-container",6)(1,xi,1,4,"ng-template",null,5,fe),t&2){let e=ue(2),i=r(3);l("ngIf",i.loaderIconTemplate||i._loaderIconTemplate)("ngIfElse",e)}}function wi(t,a){if(t&1&&(V(0,"div",11),v(1,vi,2,1,"ng-container",6)(2,bi,3,2,"ng-template",null,4,fe),R()),t&2){let e=ue(3),i=r(2);w(i.cx("loader")),l("pBind",i.ptm("loader")),d(),l("ngIf",i.loaderTemplate||i._loaderTemplate)("ngIfElse",e)}}function Ii(t,a){if(t&1){let e=ne();H(0),V(1,"div",7,1),B("scroll",function(n){T(e);let o=r();return S(o.onContainerScroll(n))}),v(3,pi,2,5,"ng-container",6)(4,ui,3,7,"ng-template",null,2,fe)(6,fi,1,4,"div",8)(7,wi,4,5,"div",9),R(),j()}if(t&2){let e=ue(5),i=r();d(),w(i.cn(i.cx("root"),i.styleClass)),l("ngStyle",i._style)("pBind",i.ptm("root")),L("id",i._id)("tabindex",i.tabindex),d(2),l("ngIf",i.contentTemplate||i._contentTemplate)("ngIfElse",e),d(3),l("ngIf",i._showSpacer),d(),l("ngIf",!i.loaderDisabled&&i._showLoader&&i.d_loading)}}function Ti(t,a){t&1&&ie(0)}function Si(t,a){if(t&1&&(H(0),v(1,Ti,1,0,"ng-container",10),j()),t&2){let e=r(2);d(),l("ngTemplateOutlet",e.contentTemplate||e._contentTemplate)("ngTemplateOutletContext",ze(5,Ze,e.items,ze(2,ci,e._items,e.loadedColumns)))}}function Ei(t,a){if(t&1&&(me(0),v(1,Si,2,8,"ng-container",16)),t&2){let e=r();d(),l("ngIf",e.contentTemplate||e._contentTemplate)}}var zi=`
.p-virtualscroller {
    position: relative;
    overflow: auto;
    contain: strict;
    transform: translateZ(0);
    will-change: scroll-position;
    outline: 0 none;
}

.p-virtualscroller-content {
    position: absolute;
    top: 0;
    left: 0;
    min-height: 100%;
    min-width: 100%;
    will-change: transform;
}

.p-virtualscroller-spacer {
    position: absolute;
    top: 0;
    left: 0;
    height: 1px;
    width: 1px;
    transform-origin: 0 0;
    pointer-events: none;
}

.p-virtualscroller-loader {
    position: sticky;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: dt('virtualscroller.loader.mask.background');
    color: dt('virtualscroller.loader.mask.color');
}

.p-virtualscroller-loader-mask {
    display: flex;
    align-items: center;
    justify-content: center;
}

.p-virtualscroller-loading-icon {
    font-size: dt('virtualscroller.loader.icon.size');
    width: dt('virtualscroller.loader.icon.size');
    height: dt('virtualscroller.loader.icon.size');
}

.p-virtualscroller-horizontal > .p-virtualscroller-content {
    display: flex;
}

.p-virtualscroller-inline .p-virtualscroller-content {
    position: static;
}
`,Di={root:({instance:t})=>["p-virtualscroller",{"p-virtualscroller-inline":t.inline,"p-virtualscroller-both p-both-scroll":t.both,"p-virtualscroller-horizontal p-horizontal-scroll":t.horizontal}],content:"p-virtualscroller-content",spacer:"p-virtualscroller-spacer",loader:({instance:t})=>["p-virtualscroller-loader",{"p-virtualscroller-loader-mask":!t.loaderTemplate}],loadingIcon:"p-virtualscroller-loading-icon"},Ot=(()=>{class t extends X{name="virtualscroller";css=zi;classes=Di;static \u0275fac=(()=>{let e;return function(n){return(e||(e=f(t)))(n||t)}})();static \u0275prov=Q({token:t,factory:t.\u0275fac})}return t})();var Mt=new q("SCROLLER_INSTANCE"),Oi=(()=>{class t extends J{zone;componentName="VirtualScroller";bindDirectiveInstance=g(C,{self:!0});$pcScroller=g(Mt,{optional:!0,skipSelf:!0})??void 0;hostName="";get id(){return this._id}set id(e){this._id=e}get style(){return this._style}set style(e){this._style=e}get styleClass(){return this._styleClass}set styleClass(e){this._styleClass=e}get tabindex(){return this._tabindex}set tabindex(e){this._tabindex=e}get items(){return this._items}set items(e){this._items=e}get itemSize(){return this._itemSize}set itemSize(e){this._itemSize=e}get scrollHeight(){return this._scrollHeight}set scrollHeight(e){this._scrollHeight=e}get scrollWidth(){return this._scrollWidth}set scrollWidth(e){this._scrollWidth=e}get orientation(){return this._orientation}set orientation(e){this._orientation=e}get step(){return this._step}set step(e){this._step=e}get delay(){return this._delay}set delay(e){this._delay=e}get resizeDelay(){return this._resizeDelay}set resizeDelay(e){this._resizeDelay=e}get appendOnly(){return this._appendOnly}set appendOnly(e){this._appendOnly=e}get inline(){return this._inline}set inline(e){this._inline=e}get lazy(){return this._lazy}set lazy(e){this._lazy=e}get disabled(){return this._disabled}set disabled(e){this._disabled=e}get loaderDisabled(){return this._loaderDisabled}set loaderDisabled(e){this._loaderDisabled=e}get columns(){return this._columns}set columns(e){this._columns=e}get showSpacer(){return this._showSpacer}set showSpacer(e){this._showSpacer=e}get showLoader(){return this._showLoader}set showLoader(e){this._showLoader=e}get numToleratedItems(){return this._numToleratedItems}set numToleratedItems(e){this._numToleratedItems=e}get loading(){return this._loading}set loading(e){this._loading=e}get autoSize(){return this._autoSize}set autoSize(e){this._autoSize=e}get trackBy(){return this._trackBy}set trackBy(e){this._trackBy=e}get options(){return this._options}set options(e){this._options=e,e&&typeof e=="object"&&(Object.entries(e).forEach(([i,n])=>this[`_${i}`]!==n&&(this[`_${i}`]=n)),Object.entries(e).forEach(([i,n])=>this[`${i}`]!==n&&(this[`${i}`]=n)))}onLazyLoad=new b;onScroll=new b;onScrollIndexChange=new b;elementViewChild;contentViewChild;height;_id;_style;_styleClass;_tabindex=0;_items;_itemSize=0;_scrollHeight;_scrollWidth;_orientation="vertical";_step=0;_delay=0;_resizeDelay=10;_appendOnly=!1;_inline=!1;_lazy=!1;_disabled=!1;_loaderDisabled=!1;_columns;_showSpacer=!0;_showLoader=!1;_numToleratedItems;_loading;_autoSize=!1;_trackBy;_options;d_loading=!1;d_numToleratedItems;contentEl;contentTemplate;itemTemplate;loaderTemplate;loaderIconTemplate;templates;_contentTemplate;_itemTemplate;_loaderTemplate;_loaderIconTemplate;first=0;last=0;page=0;isRangeChanged=!1;numItemsInViewport=0;lastScrollPos=0;lazyLoadState={};loaderArr=[];spacerStyle={};contentStyle={};scrollTimeout;resizeTimeout;initialized=!1;windowResizeListener;defaultWidth;defaultHeight;defaultContentWidth;defaultContentHeight;_contentStyleClass;get contentStyleClass(){return this._contentStyleClass}set contentStyleClass(e){this._contentStyleClass=e}get vertical(){return this._orientation==="vertical"}get horizontal(){return this._orientation==="horizontal"}get both(){return this._orientation==="both"}get loadedItems(){return this._items&&!this.d_loading?this.both?this._items.slice(this._appendOnly?0:this.first.rows,this.last.rows).map(e=>this._columns?e:Array.isArray(e)?e.slice(this._appendOnly?0:this.first.cols,this.last.cols):e):this.horizontal&&this._columns?this._items:this._items.slice(this._appendOnly?0:this.first,this.last):[]}get loadedRows(){return this.d_loading?this._loaderDisabled?this.loaderArr:[]:this.loadedItems}get loadedColumns(){return this._columns&&(this.both||this.horizontal)?this.d_loading&&this._loaderDisabled?this.both?this.loaderArr[0]:this.loaderArr:this._columns.slice(this.both?this.first.cols:this.first,this.both?this.last.cols:this.last):this._columns}_componentStyle=g(Ot);constructor(e){super(),this.zone=e}onInit(){this.setInitialState()}onChanges(e){let i=!1;if(this.scrollHeight=="100%"&&(this.height="100%"),e.loading){let{previousValue:n,currentValue:o}=e.loading;this.lazy&&n!==o&&o!==this.d_loading&&(this.d_loading=o,i=!0)}if(e.orientation&&(this.lastScrollPos=this.both?{top:0,left:0}:0),e.numToleratedItems){let{previousValue:n,currentValue:o}=e.numToleratedItems;n!==o&&o!==this.d_numToleratedItems&&(this.d_numToleratedItems=o)}if(e.options){let{previousValue:n,currentValue:o}=e.options;this.lazy&&n?.loading!==o?.loading&&o?.loading!==this.d_loading&&(this.d_loading=o.loading,i=!0),n?.numToleratedItems!==o?.numToleratedItems&&o?.numToleratedItems!==this.d_numToleratedItems&&(this.d_numToleratedItems=o.numToleratedItems)}this.initialized&&!i&&(e.items?.previousValue?.length!==e.items?.currentValue?.length||e.itemSize||e.scrollHeight||e.scrollWidth)&&(this.init(),this.calculateAutoSize())}onAfterContentInit(){this.templates.forEach(e=>{switch(e.getType()){case"content":this._contentTemplate=e.template;break;case"item":this._itemTemplate=e.template;break;case"loader":this._loaderTemplate=e.template;break;case"loadericon":this._loaderIconTemplate=e.template;break;default:this._itemTemplate=e.template;break}})}onAfterViewInit(){Promise.resolve().then(()=>{this.viewInit()})}onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptm("host")),this.initialized||this.viewInit()}onDestroy(){this.unbindResizeListener(),this.contentEl=null,this.initialized=!1}viewInit(){De(this.platformId)&&!this.initialized&&We(this.elementViewChild?.nativeElement)&&(this.setInitialState(),this.setContentEl(this.contentEl),this.init(),this.defaultWidth=_e(this.elementViewChild?.nativeElement),this.defaultHeight=ge(this.elementViewChild?.nativeElement),this.defaultContentWidth=_e(this.contentEl),this.defaultContentHeight=ge(this.contentEl),this.initialized=!0)}init(){this._disabled||(this.bindResizeListener(),setTimeout(()=>{this.setSpacerSize(),this.setSize(),this.calculateOptions(),this.cd.detectChanges()},1))}setContentEl(e){this.contentEl=e||this.contentViewChild?.nativeElement||at(this.elementViewChild?.nativeElement,".p-virtualscroller-content")}setInitialState(){this.first=this.both?{rows:0,cols:0}:0,this.last=this.both?{rows:0,cols:0}:0,this.numItemsInViewport=this.both?{rows:0,cols:0}:0,this.lastScrollPos=this.both?{top:0,left:0}:0,(this.d_loading===void 0||this.d_loading===!1)&&(this.d_loading=this._loading||!1),this.d_numToleratedItems=this._numToleratedItems,this.loaderArr=this.loaderArr.length>0?this.loaderArr:[]}getElementRef(){return this.elementViewChild}getPageByFirst(e){return Math.floor(((e??this.first)+this.d_numToleratedItems*4)/(this._step||1))}isPageChanged(e){return this._step?this.page!==this.getPageByFirst(e??this.first):!0}scrollTo(e){this.elementViewChild?.nativeElement?.scrollTo(e)}scrollToIndex(e,i="auto"){if(this.both?e.every(o=>o>-1):e>-1){let o=this.first,{scrollTop:s=0,scrollLeft:c=0}=this.elementViewChild?.nativeElement,{numToleratedItems:I}=this.calculateNumItems(),y=this.getContentPosition(),u=this.itemSize,F=(m=0,x)=>m<=x?0:m,E=(m,x,P)=>m*x+P,Z=(m=0,x=0)=>this.scrollTo({left:m,top:x,behavior:i}),M=this.both?{rows:0,cols:0}:0,ce=!1,p=!1;this.both?(M={rows:F(e[0],I[0]),cols:F(e[1],I[1])},Z(E(M.cols,u[1],y.left),E(M.rows,u[0],y.top)),p=this.lastScrollPos.top!==s||this.lastScrollPos.left!==c,ce=M.rows!==o.rows||M.cols!==o.cols):(M=F(e,I),this.horizontal?Z(E(M,u,y.left),s):Z(c,E(M,u,y.top)),p=this.lastScrollPos!==(this.horizontal?c:s),ce=M!==o),this.isRangeChanged=ce,p&&(this.first=M)}}scrollInView(e,i,n="auto"){if(i){let{first:o,viewport:s}=this.getRenderedRange(),c=(u=0,F=0)=>this.scrollTo({left:u,top:F,behavior:n}),I=i==="to-start",y=i==="to-end";if(I){if(this.both)s.first.rows-o.rows>e[0]?c(s.first.cols*this._itemSize[1],(s.first.rows-1)*this._itemSize[0]):s.first.cols-o.cols>e[1]&&c((s.first.cols-1)*this._itemSize[1],s.first.rows*this._itemSize[0]);else if(s.first-o>e){let u=(s.first-1)*this._itemSize;this.horizontal?c(u,0):c(0,u)}}else if(y){if(this.both)s.last.rows-o.rows<=e[0]+1?c(s.first.cols*this._itemSize[1],(s.first.rows+1)*this._itemSize[0]):s.last.cols-o.cols<=e[1]+1&&c((s.first.cols+1)*this._itemSize[1],s.first.rows*this._itemSize[0]);else if(s.last-o<=e+1){let u=(s.first+1)*this._itemSize;this.horizontal?c(u,0):c(0,u)}}}else this.scrollToIndex(e,n)}getRenderedRange(){let e=(o,s)=>s||o?Math.floor(o/(s||o)):0,i=this.first,n=0;if(this.elementViewChild?.nativeElement){let{scrollTop:o,scrollLeft:s}=this.elementViewChild.nativeElement;if(this.both)i={rows:e(o,this._itemSize[0]),cols:e(s,this._itemSize[1])},n={rows:i.rows+this.numItemsInViewport.rows,cols:i.cols+this.numItemsInViewport.cols};else{let c=this.horizontal?s:o;i=e(c,this._itemSize),n=i+this.numItemsInViewport}}return{first:this.first,last:this.last,viewport:{first:i,last:n}}}calculateNumItems(){let e=this.getContentPosition(),i=(this.elementViewChild?.nativeElement?this.elementViewChild.nativeElement.offsetWidth-e.left:0)||0,n=(this.elementViewChild?.nativeElement?this.elementViewChild.nativeElement.offsetHeight-e.top:0)||0,o=(y,u)=>u||y?Math.ceil(y/(u||y)):0,s=y=>Math.ceil(y/2),c=this.both?{rows:o(n,this._itemSize[0]),cols:o(i,this._itemSize[1])}:o(this.horizontal?i:n,this._itemSize),I=this.d_numToleratedItems||(this.both?[s(c.rows),s(c.cols)]:s(c));return{numItemsInViewport:c,numToleratedItems:I}}calculateOptions(){let{numItemsInViewport:e,numToleratedItems:i}=this.calculateNumItems(),n=(c,I,y,u=!1)=>this.getLast(c+I+(c<y?2:3)*y,u),o=this.first,s=this.both?{rows:n(this.first.rows,e.rows,i[0]),cols:n(this.first.cols,e.cols,i[1],!0)}:n(this.first,e,i);this.last=s,this.numItemsInViewport=e,this.d_numToleratedItems=i,this._showLoader&&(this.loaderArr=this.both?Array.from({length:e.rows}).map(()=>Array.from({length:e.cols})):Array.from({length:e})),this._lazy&&Promise.resolve().then(()=>{this.lazyLoadState={first:this._step?this.both?{rows:0,cols:o.cols}:0:o,last:Math.min(this._step?this._step:this.last,this._items.length)},this.handleEvents("onLazyLoad",this.lazyLoadState)})}calculateAutoSize(){this._autoSize&&!this.d_loading&&Promise.resolve().then(()=>{if(this.contentEl){this.contentEl.style.minHeight=this.contentEl.style.minWidth="auto",this.contentEl.style.position="relative",this.elementViewChild.nativeElement.style.contain="none";let[e,i]=[_e(this.contentEl),ge(this.contentEl)];e!==this.defaultContentWidth&&(this.elementViewChild.nativeElement.style.width=""),i!==this.defaultContentHeight&&(this.elementViewChild.nativeElement.style.height="");let[n,o]=[_e(this.elementViewChild.nativeElement),ge(this.elementViewChild.nativeElement)];(this.both||this.horizontal)&&(this.elementViewChild.nativeElement.style.width=n<this.defaultWidth?n+"px":this._scrollWidth||this.defaultWidth+"px"),(this.both||this.vertical)&&(this.elementViewChild.nativeElement.style.height=o<this.defaultHeight?o+"px":this._scrollHeight||this.defaultHeight+"px"),this.contentEl.style.minHeight=this.contentEl.style.minWidth="",this.contentEl.style.position="",this.elementViewChild.nativeElement.style.contain=""}})}getLast(e=0,i=!1){return this._items?Math.min(i?(this._columns||this._items[0]).length:this._items.length,e):0}getContentPosition(){if(this.contentEl){let e=getComputedStyle(this.contentEl),i=parseFloat(e.paddingLeft)+Math.max(parseFloat(e.left)||0,0),n=parseFloat(e.paddingRight)+Math.max(parseFloat(e.right)||0,0),o=parseFloat(e.paddingTop)+Math.max(parseFloat(e.top)||0,0),s=parseFloat(e.paddingBottom)+Math.max(parseFloat(e.bottom)||0,0);return{left:i,right:n,top:o,bottom:s,x:i+n,y:o+s}}return{left:0,right:0,top:0,bottom:0,x:0,y:0}}setSize(){if(this.elementViewChild?.nativeElement){let e=this.elementViewChild.nativeElement,i=e.parentElement?.parentElement,n=e.offsetWidth,o=i?.offsetWidth||0,s=this._scrollWidth||`${n||o}px`,c=e.offsetHeight,I=i?.offsetHeight||0,y=this._scrollHeight||`${c||I}px`,u=(F,E)=>e.style[F]=E;this.both||this.horizontal?(u("height",y),u("width",s)):u("height",y)}}setSpacerSize(){if(this._items){let e=this.getContentPosition(),i=(n,o,s,c=0)=>this.spacerStyle=ye(k({},this.spacerStyle),{[`${n}`]:(o||[]).length*s+c+"px"});this.both?(i("height",this._items,this._itemSize[0],e.y),i("width",this._columns||this._items[1],this._itemSize[1],e.x)):this.horizontal?i("width",this._columns||this._items,this._itemSize,e.x):i("height",this._items,this._itemSize,e.y)}}setContentPosition(e){if(this.contentEl&&!this._appendOnly){let i=e?e.first:this.first,n=(s,c)=>s*c,o=(s=0,c=0)=>this.contentStyle=ye(k({},this.contentStyle),{transform:`translate3d(${s}px, ${c}px, 0)`});if(this.both)o(n(i.cols,this._itemSize[1]),n(i.rows,this._itemSize[0]));else{let s=n(i,this._itemSize);this.horizontal?o(s,0):o(0,s)}}}onScrollPositionChange(e){let i=e.target;if(!i)throw new Error("Event target is null");let n=this.getContentPosition(),o=(p,m)=>p?p>m?p-m:p:0,s=(p,m)=>m||p?Math.floor(p/(m||p)):0,c=(p,m,x,P,ee,de)=>p<=ee?ee:de?x-P-ee:m+ee-1,I=(p,m,x,P,ee,de,Me)=>p<=de?0:Math.max(0,Me?p<m?x:p-de:p>m?x:p-2*de),y=(p,m,x,P,ee,de=!1)=>{let Me=m+P+2*ee;return p>=ee&&(Me+=ee+1),this.getLast(Me,de)},u=o(i.scrollTop,n.top),F=o(i.scrollLeft,n.left),E=this.both?{rows:0,cols:0}:0,Z=this.last,M=!1,ce=this.lastScrollPos;if(this.both){let p=this.lastScrollPos.top<=u,m=this.lastScrollPos.left<=F;if(!this._appendOnly||this._appendOnly&&(p||m)){let x={rows:s(u,this._itemSize[0]),cols:s(F,this._itemSize[1])},P={rows:c(x.rows,this.first.rows,this.last.rows,this.numItemsInViewport.rows,this.d_numToleratedItems[0],p),cols:c(x.cols,this.first.cols,this.last.cols,this.numItemsInViewport.cols,this.d_numToleratedItems[1],m)};E={rows:I(x.rows,P.rows,this.first.rows,this.last.rows,this.numItemsInViewport.rows,this.d_numToleratedItems[0],p),cols:I(x.cols,P.cols,this.first.cols,this.last.cols,this.numItemsInViewport.cols,this.d_numToleratedItems[1],m)},Z={rows:y(x.rows,E.rows,this.last.rows,this.numItemsInViewport.rows,this.d_numToleratedItems[0]),cols:y(x.cols,E.cols,this.last.cols,this.numItemsInViewport.cols,this.d_numToleratedItems[1],!0)},M=E.rows!==this.first.rows||Z.rows!==this.last.rows||E.cols!==this.first.cols||Z.cols!==this.last.cols||this.isRangeChanged,ce={top:u,left:F}}}else{let p=this.horizontal?F:u,m=this.lastScrollPos<=p;if(!this._appendOnly||this._appendOnly&&m){let x=s(p,this._itemSize),P=c(x,this.first,this.last,this.numItemsInViewport,this.d_numToleratedItems,m);E=I(x,P,this.first,this.last,this.numItemsInViewport,this.d_numToleratedItems,m),Z=y(x,E,this.last,this.numItemsInViewport,this.d_numToleratedItems),M=E!==this.first||Z!==this.last||this.isRangeChanged,ce=p}}return{first:E,last:Z,isRangeChanged:M,scrollPos:ce}}onScrollChange(e){let{first:i,last:n,isRangeChanged:o,scrollPos:s}=this.onScrollPositionChange(e);if(o){let c={first:i,last:n};if(this.setContentPosition(c),this.first=i,this.last=n,this.lastScrollPos=s,this.handleEvents("onScrollIndexChange",c),this._lazy&&this.isPageChanged(i)){let I={first:this._step?Math.min(this.getPageByFirst(i)*this._step,this._items.length-this._step):i,last:Math.min(this._step?(this.getPageByFirst(i)+1)*this._step:n,this._items.length)};(this.lazyLoadState.first!==I.first||this.lazyLoadState.last!==I.last)&&this.handleEvents("onLazyLoad",I),this.lazyLoadState=I}}}onContainerScroll(e){if(this.handleEvents("onScroll",{originalEvent:e}),this._delay){if(this.scrollTimeout&&clearTimeout(this.scrollTimeout),!this.d_loading&&this._showLoader){let{isRangeChanged:i}=this.onScrollPositionChange(e);(i||this._step&&this.isPageChanged())&&(this.d_loading=!0,this.cd.detectChanges())}this.scrollTimeout=setTimeout(()=>{this.onScrollChange(e),this.d_loading&&this._showLoader&&(!this._lazy||this._loading===void 0)&&(this.d_loading=!1,this.page=this.getPageByFirst()),this.cd.detectChanges()},this._delay)}else!this.d_loading&&this.onScrollChange(e)}bindResizeListener(){De(this.platformId)&&(this.windowResizeListener||this.zone.runOutsideAngular(()=>{let e=this.document.defaultView,i=ve()?"orientationchange":"resize";this.windowResizeListener=this.renderer.listen(e,i,this.onWindowResize.bind(this))}))}unbindResizeListener(){this.windowResizeListener&&(this.windowResizeListener(),this.windowResizeListener=null)}onWindowResize(){this.resizeTimeout&&clearTimeout(this.resizeTimeout),this.resizeTimeout=setTimeout(()=>{if(We(this.elementViewChild?.nativeElement)){let[e,i]=[_e(this.elementViewChild?.nativeElement),ge(this.elementViewChild?.nativeElement)],[n,o]=[e!==this.defaultWidth,i!==this.defaultHeight];(this.both?n||o:this.horizontal?n:this.vertical&&o)&&this.zone.run(()=>{this.d_numToleratedItems=this._numToleratedItems,this.defaultWidth=e,this.defaultHeight=i,this.defaultContentWidth=_e(this.contentEl),this.defaultContentHeight=ge(this.contentEl),this.init()})}},this._resizeDelay)}handleEvents(e,i){return this.options&&this.options[e]?this.options[e](i):this[e].emit(i)}getContentOptions(){return{contentStyleClass:`p-virtualscroller-content ${this.d_loading?"p-virtualscroller-loading":""}`,items:this.loadedItems,getItemOptions:e=>this.getOptions(e),loading:this.d_loading,getLoaderOptions:(e,i)=>this.getLoaderOptions(e,i),itemSize:this._itemSize,rows:this.loadedRows,columns:this.loadedColumns,spacerStyle:this.spacerStyle,contentStyle:this.contentStyle,vertical:this.vertical,horizontal:this.horizontal,both:this.both,scrollTo:this.scrollTo.bind(this),scrollToIndex:this.scrollToIndex.bind(this),orientation:this._orientation,scrollableElement:this.elementViewChild?.nativeElement}}getOptions(e){let i=(this._items||[]).length,n=this.both?this.first.rows+e:this.first+e;return{index:n,count:i,first:n===0,last:n===i-1,even:n%2===0,odd:n%2!==0}}getLoaderOptions(e,i){let n=this.loaderArr.length;return k({index:e,count:n,first:e===0,last:e===n-1,even:e%2===0,odd:e%2!==0,loading:this.d_loading},i)}static \u0275fac=function(i){return new(i||t)(Se(ke))};static \u0275cmp=z({type:t,selectors:[["p-scroller"],["p-virtualscroller"],["p-virtual-scroller"],["p-virtualScroller"]],contentQueries:function(i,n,o){if(i&1&&be(o,Dt,4)(o,ii,4)(o,ni,4)(o,oi,4)(o,Te,4),i&2){let s;D(s=O())&&(n.contentTemplate=s.first),D(s=O())&&(n.itemTemplate=s.first),D(s=O())&&(n.loaderTemplate=s.first),D(s=O())&&(n.loaderIconTemplate=s.first),D(s=O())&&(n.templates=s)}},viewQuery:function(i,n){if(i&1&&Ve(si,5)(Dt,5),i&2){let o;D(o=O())&&(n.elementViewChild=o.first),D(o=O())&&(n.contentViewChild=o.first)}},hostVars:2,hostBindings:function(i,n){i&2&&Ge("height",n.height)},inputs:{hostName:"hostName",id:"id",style:"style",styleClass:"styleClass",tabindex:"tabindex",items:"items",itemSize:"itemSize",scrollHeight:"scrollHeight",scrollWidth:"scrollWidth",orientation:"orientation",step:"step",delay:"delay",resizeDelay:"resizeDelay",appendOnly:"appendOnly",inline:"inline",lazy:"lazy",disabled:"disabled",loaderDisabled:"loaderDisabled",columns:"columns",showSpacer:"showSpacer",showLoader:"showLoader",numToleratedItems:"numToleratedItems",loading:"loading",autoSize:"autoSize",trackBy:"trackBy",options:"options"},outputs:{onLazyLoad:"onLazyLoad",onScroll:"onScroll",onScrollIndexChange:"onScrollIndexChange"},features:[U([Ot,{provide:Mt,useExisting:t},{provide:Y,useExisting:t}]),K([C]),_],ngContentSelectors:ri,decls:3,vars:2,consts:[["disabledContainer",""],["element",""],["buildInContent",""],["content",""],["buildInLoader",""],["buildInLoaderIcon",""],[4,"ngIf","ngIfElse"],[3,"scroll","ngStyle","pBind"],[3,"class","ngStyle","pBind",4,"ngIf"],[3,"class","pBind",4,"ngIf"],[4,"ngTemplateOutlet","ngTemplateOutletContext"],[3,"pBind"],[4,"ngFor","ngForOf","ngForTrackBy"],[3,"ngStyle","pBind"],[4,"ngFor","ngForOf"],["data-p-icon","spinner",3,"spin","pBind"],[4,"ngIf"]],template:function(i,n){if(i&1&&(xe(),v(0,Ii,8,10,"ng-container",6)(1,Ei,2,1,"ng-template",null,0,fe)),i&2){let o=ue(2);l("ngIf",!n._disabled)("ngIfElse",o)}},dependencies:[le,et,we,Ie,tt,ut,G,C],encapsulation:2})}return t})(),Io=(()=>{class t{static \u0275fac=function(i){return new(i||t)};static \u0275mod=he({type:t});static \u0275inj=pe({imports:[Oi,G,G]})}return t})();var Vt=`
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
`;var Mi={root:{position:"relative"}},ki={root:({instance:t})=>["p-skeleton p-component",{"p-skeleton-circle":t.shape==="circle","p-skeleton-animation-none":t.animation==="none"}]},Lt=(()=>{class t extends X{name="skeleton";style=Vt;classes=ki;inlineStyles=Mi;static \u0275fac=(()=>{let e;return function(n){return(e||(e=f(t)))(n||t)}})();static \u0275prov=Q({token:t,factory:t.\u0275fac})}return t})();var Bt=new q("SKELETON_INSTANCE"),jo=(()=>{class t extends J{componentName="Skeleton";$pcSkeleton=g(Bt,{optional:!0,skipSelf:!0})??void 0;bindDirectiveInstance=g(C,{self:!0});onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptms(["host","root"]))}styleClass;shape="rectangle";animation="wave";borderRadius;size;width="100%";height="1rem";_componentStyle=g(Lt);get containerStyle(){let e=this._componentStyle?.inlineStyles.root,i;return this.$unstyled()||(this.size?i=ye(k({},e),{width:this.size,height:this.size,borderRadius:this.borderRadius}):i=ye(k({},e),{width:this.width,height:this.height,borderRadius:this.borderRadius})),i}get dataP(){return this.cn({[this.shape]:this.shape})}static \u0275fac=(()=>{let e;return function(n){return(e||(e=f(t)))(n||t)}})();static \u0275cmp=z({type:t,selectors:[["p-skeleton"]],hostVars:6,hostBindings:function(i,n){i&2&&(L("aria-hidden",!0)("data-p",n.dataP),se(n.containerStyle),w(n.cn(n.cx("root"),n.styleClass)))},inputs:{styleClass:"styleClass",shape:"shape",animation:"animation",borderRadius:"borderRadius",size:"size",width:"width",height:"height"},features:[U([Lt,{provide:Bt,useExisting:t},{provide:Y,useExisting:t}]),K([C]),_],decls:0,vars:0,template:function(i,n){},dependencies:[le,G],encapsulation:2,changeDetection:0})}return t})();var Uo=(()=>{class t extends xt{pcFluid=g(Be,{optional:!0,host:!0,skipSelf:!0});fluid=h(void 0,{transform:A});variant=h();size=h();inputSize=h();pattern=h();min=h();max=h();step=h();minlength=h();maxlength=h();$variant=$(()=>this.variant()||this.config.inputStyle()||this.config.inputVariant());get hasFluid(){return this.fluid()??!!this.pcFluid}static \u0275fac=(()=>{let e;return function(n){return(e||(e=f(t)))(n||t)}})();static \u0275dir=oe({type:t,inputs:{fluid:[1,"fluid"],variant:[1,"variant"],size:[1,"size"],inputSize:[1,"inputSize"],pattern:[1,"pattern"],min:[1,"min"],max:[1,"max"],step:[1,"step"],minlength:[1,"minlength"],maxlength:[1,"maxlength"]},features:[_]})}return t})();var Ft=`
    .p-chip {
        display: inline-flex;
        align-items: center;
        background: dt('chip.background');
        color: dt('chip.color');
        border-radius: dt('chip.border.radius');
        padding-block: dt('chip.padding.y');
        padding-inline: dt('chip.padding.x');
        gap: dt('chip.gap');
    }

    .p-chip-icon {
        color: dt('chip.icon.color');
        font-size: dt('chip.icon.size');
        width: dt('chip.icon.size');
        height: dt('chip.icon.size');
    }

    .p-chip-image {
        border-radius: 50%;
        width: dt('chip.image.width');
        height: dt('chip.image.height');
        margin-inline-start: calc(-1 * dt('chip.padding.y'));
    }

    .p-chip:has(.p-chip-remove-icon) {
        padding-inline-end: dt('chip.padding.y');
    }

    .p-chip:has(.p-chip-image) {
        padding-block-start: calc(dt('chip.padding.y') / 2);
        padding-block-end: calc(dt('chip.padding.y') / 2);
    }

    .p-chip-remove-icon {
        cursor: pointer;
        font-size: dt('chip.remove.icon.size');
        width: dt('chip.remove.icon.size');
        height: dt('chip.remove.icon.size');
        color: dt('chip.remove.icon.color');
        border-radius: 50%;
        transition:
            outline-color dt('chip.transition.duration'),
            box-shadow dt('chip.transition.duration');
        outline-color: transparent;
    }

    .p-chip-remove-icon:focus-visible {
        box-shadow: dt('chip.remove.icon.focus.ring.shadow');
        outline: dt('chip.remove.icon.focus.ring.width') dt('chip.remove.icon.focus.ring.style') dt('chip.remove.icon.focus.ring.color');
        outline-offset: dt('chip.remove.icon.focus.ring.offset');
    }
`;var Vi=["removeicon"],Li=["*"];function Bi(t,a){if(t&1){let e=ne();V(0,"img",4),B("error",function(n){T(e);let o=r();return S(o.imageError(n))}),R()}if(t&2){let e=r();w(e.cx("image")),l("pBind",e.ptm("image"))("src",e.image,Qe)("alt",e.alt)}}function Fi(t,a){if(t&1&&Ee(0,"span",6),t&2){let e=r(2);w(e.icon),l("pBind",e.ptm("icon"))("ngClass",e.cx("icon"))}}function Ni(t,a){if(t&1&&v(0,Fi,1,4,"span",5),t&2){let e=r();l("ngIf",e.icon)}}function Ri(t,a){if(t&1&&(V(0,"div",7),Xe(1),R()),t&2){let e=r();w(e.cx("label")),l("pBind",e.ptm("label")),d(),Ye(e.label)}}function Ai(t,a){if(t&1){let e=ne();V(0,"span",11),B("click",function(n){T(e);let o=r(3);return S(o.close(n))})("keydown",function(n){T(e);let o=r(3);return S(o.onKeydown(n))}),R()}if(t&2){let e=r(3);w(e.removeIcon),l("pBind",e.ptm("removeIcon"))("ngClass",e.cx("removeIcon")),L("tabindex",e.disabled?-1:0)("aria-label",e.removeAriaLabel)}}function Pi(t,a){if(t&1){let e=ne();N(),V(0,"svg",12),B("click",function(n){T(e);let o=r(3);return S(o.close(n))})("keydown",function(n){T(e);let o=r(3);return S(o.onKeydown(n))}),R()}if(t&2){let e=r(3);w(e.cx("removeIcon")),l("pBind",e.ptm("removeIcon")),L("tabindex",e.disabled?-1:0)("aria-label",e.removeAriaLabel)}}function Hi(t,a){if(t&1&&(H(0),v(1,Ai,1,6,"span",9)(2,Pi,1,5,"svg",10),j()),t&2){let e=r(2);d(),l("ngIf",e.removeIcon),d(),l("ngIf",!e.removeIcon)}}function ji(t,a){}function $i(t,a){t&1&&v(0,ji,0,0,"ng-template")}function Wi(t,a){if(t&1){let e=ne();V(0,"span",13),B("click",function(n){T(e);let o=r(2);return S(o.close(n))})("keydown",function(n){T(e);let o=r(2);return S(o.onKeydown(n))}),v(1,$i,1,0,null,14),R()}if(t&2){let e=r(2);w(e.cx("removeIcon")),l("pBind",e.ptm("removeIcon")),L("tabindex",e.disabled?-1:0)("aria-label",e.removeAriaLabel),d(),l("ngTemplateOutlet",e.removeIconTemplate||e._removeIconTemplate)}}function Zi(t,a){if(t&1&&(H(0),v(1,Hi,3,2,"ng-container",3)(2,Wi,2,6,"span",8),j()),t&2){let e=r();d(),l("ngIf",!e.removeIconTemplate&&!e._removeIconTemplate),d(),l("ngIf",e.removeIconTemplate||e._removeIconTemplate)}}var Qi={root:({instance:t})=>({display:!t.visible&&"none"})},qi={root:({instance:t})=>["p-chip p-component",{"p-disabled":t.disabled}],image:"p-chip-image",icon:"p-chip-icon",label:"p-chip-label",removeIcon:"p-chip-remove-icon"},Nt=(()=>{class t extends X{name="chip";style=Ft;classes=qi;inlineStyles=Qi;static \u0275fac=(()=>{let e;return function(n){return(e||(e=f(t)))(n||t)}})();static \u0275prov=Q({token:t,factory:t.\u0275fac})}return t})();var Rt=new q("CHIP_INSTANCE"),fs=(()=>{class t extends J{componentName="Chip";$pcChip=g(Rt,{optional:!0,skipSelf:!0})??void 0;bindDirectiveInstance=g(C,{self:!0});onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptms(["host","root"]))}label;icon;image;alt;styleClass;disabled=!1;removable=!1;removeIcon;onRemove=new b;onImageError=new b;visible=!0;get removeAriaLabel(){return this.config.getTranslation(ht.ARIA).removeLabel}get chipProps(){return this._chipProps}set chipProps(e){this._chipProps=e,e&&typeof e=="object"&&Object.entries(e).forEach(([i,n])=>this[`_${i}`]!==n&&(this[`_${i}`]=n))}_chipProps;_componentStyle=g(Nt);removeIconTemplate;templates;_removeIconTemplate;onAfterContentInit(){this.templates.forEach(e=>{e.getType()==="removeicon"?this._removeIconTemplate=e.template:this._removeIconTemplate=e.template})}onChanges(e){if(e.chipProps&&e.chipProps.currentValue){let{currentValue:i}=e.chipProps;i.label!==void 0&&(this.label=i.label),i.icon!==void 0&&(this.icon=i.icon),i.image!==void 0&&(this.image=i.image),i.alt!==void 0&&(this.alt=i.alt),i.styleClass!==void 0&&(this.styleClass=i.styleClass),i.removable!==void 0&&(this.removable=i.removable),i.removeIcon!==void 0&&(this.removeIcon=i.removeIcon)}}close(e){this.visible=!1,this.onRemove.emit(e)}onKeydown(e){(e.key==="Enter"||e.key==="Backspace")&&this.close(e)}imageError(e){this.onImageError.emit(e)}get dataP(){return this.cn({removable:this.removable})}static \u0275fac=(()=>{let e;return function(n){return(e||(e=f(t)))(n||t)}})();static \u0275cmp=z({type:t,selectors:[["p-chip"]],contentQueries:function(i,n,o){if(i&1&&be(o,Vi,4)(o,Te,4),i&2){let s;D(s=O())&&(n.removeIconTemplate=s.first),D(s=O())&&(n.templates=s)}},hostVars:6,hostBindings:function(i,n){i&2&&(L("aria-label",n.label)("data-p",n.dataP),se(n.sx("root")),w(n.cn(n.cx("root"),n.styleClass)))},inputs:{label:"label",icon:"icon",image:"image",alt:"alt",styleClass:"styleClass",disabled:[2,"disabled","disabled",A],removable:[2,"removable","removable",A],removeIcon:"removeIcon",chipProps:"chipProps"},outputs:{onRemove:"onRemove",onImageError:"onImageError"},features:[U([Nt,{provide:Rt,useExisting:t},{provide:Y,useExisting:t}]),K([C]),_],ngContentSelectors:Li,decls:6,vars:4,consts:[["iconTemplate",""],[3,"pBind","class","src","alt","error",4,"ngIf","ngIfElse"],[3,"pBind","class",4,"ngIf"],[4,"ngIf"],[3,"error","pBind","src","alt"],[3,"pBind","class","ngClass",4,"ngIf"],[3,"pBind","ngClass"],[3,"pBind"],["role","button",3,"pBind","class","click","keydown",4,"ngIf"],["role","button",3,"pBind","class","ngClass","click","keydown",4,"ngIf"],["data-p-icon","times-circle","role","button",3,"pBind","class","click","keydown",4,"ngIf"],["role","button",3,"click","keydown","pBind","ngClass"],["data-p-icon","times-circle","role","button",3,"click","keydown","pBind"],["role","button",3,"click","keydown","pBind"],[4,"ngTemplateOutlet"]],template:function(i,n){if(i&1&&(xe(),me(0),v(1,Bi,1,5,"img",1)(2,Ni,1,1,"ng-template",null,0,fe)(4,Ri,2,4,"div",2)(5,Zi,3,2,"ng-container",3)),i&2){let o=ue(3);d(),l("ngIf",n.image)("ngIfElse",o),d(3),l("ngIf",n.label),d(),l("ngIf",n.removable)}},dependencies:[le,Je,we,Ie,zt,G,C],encapsulation:2,changeDetection:0})}return t})();export{Jn as a,vn as b,xn as c,zt as d,Ne as e,un as f,fn as g,xt as h,fs as i,Un as j,Oi as k,Io as l,jo as m,Uo as n};
/**i18n:22e098ff1cbf4220c99e0cb12af57cfa6bdd822e90f8e9443f8715589c7ae7f3*/
