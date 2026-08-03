import{a as ge,x as Ae}from"./chunk-MZ63JWLS.js";import{c as me}from"./chunk-WTSZEFB7.js";import{c as be,e as ye,i as j}from"./chunk-VDXMXSPO.js";import{Q as fe,T as $,V as D,W as I,aa as c,ba as R,ia as he,la as ve,t as x,u as q,v as B}from"./chunk-VQATFZMM.js";import{C as Y,G as K}from"./chunk-YLKOL3QT.js";import{g as le,i as ue,l as C}from"./chunk-IXCWRJJ2.js";import{$ as Z,$b as V,Ab as h,Eb as G,Ec as pe,Fb as W,Fc as H,Hb as ie,Ic as Q,Ob as k,Pa as s,Q as T,Qb as v,R as L,Rb as F,Sb as O,Tb as re,U as y,Vb as ae,W as r,Wb as ce,_b as de,aa as ee,ba as w,bb as g,cc as d,fb as A,ga as z,gb as _,hb as N,kc as E,la as ne,mc as se,ob as u,qa as m,rb as oe,sb as te,xb as a,yb as S,zb as P,zc as p}from"./chunk-TI44TR7P.js";import{a as M}from"./chunk-B4FWF4YO.js";var _e=`
    .p-accordionpanel {
        display: flex;
        flex-direction: column;
        border-style: solid;
        border-width: dt('accordion.panel.border.width');
        border-color: dt('accordion.panel.border.color');
    }

    .p-accordionheader {
        all: unset;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: dt('accordion.header.padding');
        color: dt('accordion.header.color');
        background: dt('accordion.header.background');
        border-style: solid;
        border-width: dt('accordion.header.border.width');
        border-color: dt('accordion.header.border.color');
        font-weight: dt('accordion.header.font.weight');
        border-radius: dt('accordion.header.border.radius');
        transition:
            background dt('accordion.transition.duration'),
            color dt('accordion.transition.duration'),
            outline-color dt('accordion.transition.duration'),
            box-shadow dt('accordion.transition.duration');
        outline-color: transparent;
    }

    .p-accordionpanel:first-child > .p-accordionheader {
        border-width: dt('accordion.header.first.border.width');
        border-start-start-radius: dt('accordion.header.first.top.border.radius');
        border-start-end-radius: dt('accordion.header.first.top.border.radius');
    }

    .p-accordionpanel:last-child > .p-accordionheader {
        border-end-start-radius: dt('accordion.header.last.bottom.border.radius');
        border-end-end-radius: dt('accordion.header.last.bottom.border.radius');
    }

    .p-accordionpanel:last-child.p-accordionpanel-active > .p-accordionheader {
        border-end-start-radius: dt('accordion.header.last.active.bottom.border.radius');
        border-end-end-radius: dt('accordion.header.last.active.bottom.border.radius');
    }

    .p-accordionheader-toggle-icon {
        color: dt('accordion.header.toggle.icon.color');
    }

    .p-accordionpanel:not(.p-disabled) .p-accordionheader:focus-visible {
        box-shadow: dt('accordion.header.focus.ring.shadow');
        outline: dt('accordion.header.focus.ring.width') dt('accordion.header.focus.ring.style') dt('accordion.header.focus.ring.color');
        outline-offset: dt('accordion.header.focus.ring.offset');
    }

    .p-accordionpanel:not(.p-accordionpanel-active):not(.p-disabled) > .p-accordionheader:hover {
        background: dt('accordion.header.hover.background');
        color: dt('accordion.header.hover.color');
    }

    .p-accordionpanel:not(.p-accordionpanel-active):not(.p-disabled) .p-accordionheader:hover .p-accordionheader-toggle-icon {
        color: dt('accordion.header.toggle.icon.hover.color');
    }

    .p-accordionpanel:not(.p-disabled).p-accordionpanel-active > .p-accordionheader {
        background: dt('accordion.header.active.background');
        color: dt('accordion.header.active.color');
    }

    .p-accordionpanel:not(.p-disabled).p-accordionpanel-active > .p-accordionheader .p-accordionheader-toggle-icon {
        color: dt('accordion.header.toggle.icon.active.color');
    }

    .p-accordionpanel:not(.p-disabled).p-accordionpanel-active > .p-accordionheader:hover {
        background: dt('accordion.header.active.hover.background');
        color: dt('accordion.header.active.hover.color');
    }

    .p-accordionpanel:not(.p-disabled).p-accordionpanel-active > .p-accordionheader:hover .p-accordionheader-toggle-icon {
        color: dt('accordion.header.toggle.icon.active.hover.color');
    }

    .p-accordioncontent {
        display: grid;
        grid-template-rows: 1fr;
    }

    .p-accordioncontent-wrapper {
        min-height: 0;
    }

    .p-accordioncontent-content {
        border-style: solid;
        border-width: dt('accordion.content.border.width');
        border-color: dt('accordion.content.border.color');
        background-color: dt('accordion.content.background');
        color: dt('accordion.content.color');
        padding: dt('accordion.content.padding');
    }
`;var U=["*"],He=["toggleicon"],Be=o=>({active:o});function Re(o,l){}function Le(o,l){o&1&&N(0,Re,0,0,"ng-template");}function Ve(o,l){if(o&1&&N(0,Le,1,0,null,0),o&2){let e=v();a("ngTemplateOutlet",e.toggleicon)("ngTemplateOutletContext",se(2,Be,e.active()));}}function Ke(o,l){if(o&1&&h(0,"span",4),o&2){let e=v(3);d(e.cn(e.cx("toggleicon"),e.pcAccordion.collapseIcon)),a("pBind",e.ptm("toggleicon")),u("aria-hidden",!0);}}function $e(o,l){if(o&1&&(w(),h(0,"svg",5)),o&2){let e=v(3);d(e.cx("toggleicon")),a("pBind",e.ptm("toggleicon")),u("aria-hidden",!0);}}function je(o,l){if(o&1&&(G(0),N(1,Ke,1,4,"span",2)(2,$e,1,4,"svg",3),W()),o&2){let e=v(2);s(),a("ngIf",e.pcAccordion.collapseIcon),s(),a("ngIf",!e.pcAccordion.collapseIcon);}}function Ue(o,l){if(o&1&&h(0,"span",4),o&2){let e=v(3);d(e.cn(e.cx("toggleicon"),e.pcAccordion.expandIcon)),a("pBind",e.ptm("toggleicon")),u("aria-hidden",!0);}}function ze(o,l){if(o&1&&(w(),h(0,"svg",7)),o&2){let e=v(3);a("pBind",e.ptm("toggleicon")),u("aria-hidden",!0);}}function Ge(o,l){if(o&1&&(G(0),N(1,Ue,1,4,"span",2)(2,ze,1,2,"svg",6),W()),o&2){let e=v(2);s(),a("ngIf",e.pcAccordion.expandIcon),s(),a("ngIf",!e.pcAccordion.expandIcon);}}function We(o,l){if(o&1&&N(0,je,3,2,"ng-container",1)(1,Ge,3,2,"ng-container",1),o&2){let e=v();a("ngIf",e.active()),s(),a("ngIf",!e.active());}}var Qe=`
${_e}

/* For PrimeNG */
.p-accordionheader-toggle-icon.icon-start {
    order: -1;
}

.p-accordionheader:has(.p-accordionheader-toggle-icon.icon-start) {
    justify-content: flex-start;
    gap: dt('accordion.header.padding');
}

.p-accordionheader.p-ripple {
    overflow: hidden;
    position: relative;
}

.p-accordioncontent .p-motion {
    display: grid;
    grid-template-rows: 1fr;
}
`,qe={root:"p-accordion p-component",panel:({instance:o})=>["p-accordionpanel",{"p-accordionpanel-active":o.active(),"p-disabled":o.disabled()}],header:"p-accordionheader",toggleicon:"p-accordionheader-toggle-icon",contentContainer:"p-accordioncontent",contentWrapper:"p-accordioncontent-wrapper",content:"p-accordioncontent-content"},b=(()=>{class o extends ${name="accordion";style=Qe;classes=qe;static ɵfac=(()=>{let e;return function(n){return(e||(e=m(o)))(n||o);};})();static ɵprov=L({token:o,factory:o.ɵfac});}return o;})();var Ee=new y("ACCORDION_PANEL_INSTANCE"),Ce=new y("ACCORDION_HEADER_INSTANCE"),De=new y("ACCORDION_CONTENT_INSTANCE"),Ie=new y("ACCORDION_INSTANCE"),Ne=(()=>{class o extends I{$pcAccordionPanel=r(Ee,{optional:!0,skipSelf:!0})??void 0;bindDirectiveInstance=r(c,{self:!0});componentName="AccordionPanel";onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptm("root"));}pcAccordion=r(T(()=>J));value=Q(void 0);disabled=H(!1,{transform:e=>j(e)});active=p(()=>this.pcAccordion.multiple()?this.valueEquals(this.pcAccordion.value(),this.value()):this.pcAccordion.value()===this.value());valueEquals(e,t){return Array.isArray(e)?e.includes(t):e===t;}_componentStyle=r(b);static ɵfac=(()=>{let e;return function(n){return(e||(e=m(o)))(n||o);};})();static ɵcmp=g({type:o,selectors:[["p-accordion-panel"],["p-accordionpanel"]],hostVars:4,hostBindings:function(t,n){t&2&&(u("data-p-disabled",n.disabled())("data-p-active",n.active()),d(n.cx("panel")));},inputs:{value:[1,"value"],disabled:[1,"disabled"]},outputs:{value:"valueChange"},features:[E([b,{provide:Ee,useExisting:o},{provide:D,useExisting:o}]),A([c]),_],ngContentSelectors:U,decls:1,vars:0,template:function(t,n){t&1&&(F(),O(0));},dependencies:[C,R],encapsulation:2,changeDetection:0});}return o;})(),En=(()=>{class o extends I{$pcAccordionHeader=r(Ce,{optional:!0,skipSelf:!0})??void 0;bindDirectiveInstance=r(c,{self:!0});componentName="AccordionHeader";onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptm("root"));}pcAccordion=r(T(()=>J));pcAccordionPanel=r(T(()=>Ne));id=p(()=>`${this.pcAccordion.id()}_accordionheader_${this.pcAccordionPanel.value()}`);active=p(()=>this.pcAccordionPanel.active());disabled=p(()=>this.pcAccordionPanel.disabled());ariaControls=p(()=>`${this.pcAccordion.id()}_accordioncontent_${this.pcAccordionPanel.value()}`);toggleicon;onClick(e){if(this.disabled())return;let t=this.active();this.changeActiveValue();let n=this.active(),i=this.pcAccordionPanel.value();!t&&n?this.pcAccordion.onOpen.emit({originalEvent:e,index:i}):t&&!n&&this.pcAccordion.onClose.emit({originalEvent:e,index:i});}onFocus(){!this.disabled()&&this.pcAccordion.selectOnFocus()&&this.changeActiveValue();}onKeydown(e){switch(e.code){case"ArrowDown":this.arrowDownKey(e);break;case"ArrowUp":this.arrowUpKey(e);break;case"Home":this.onHomeKey(e);break;case"End":this.onEndKey(e);break;case"Enter":case"Space":case"NumpadEnter":this.onEnterKey(e);break;default:break;}}_componentStyle=r(b);changeActiveValue(){this.pcAccordion.updateValue(this.pcAccordionPanel.value());}findPanel(e){return e?.closest('[data-pc-name="accordionpanel"]');}findHeader(e){return x(e,'[data-pc-name="accordionheader"]');}findNextPanel(e,t=!1){let n=t?e:e.nextElementSibling;return n?B(n,"data-p-disabled")?this.findNextPanel(n):this.findHeader(n):null;}findPrevPanel(e,t=!1){let n=t?e:e.previousElementSibling;return n?B(n,"data-p-disabled")?this.findPrevPanel(n):this.findHeader(n):null;}findFirstPanel(){return this.findNextPanel(this.pcAccordion.el.nativeElement.firstElementChild,!0);}findLastPanel(){return this.findPrevPanel(this.pcAccordion.el.nativeElement.lastElementChild,!0);}changeFocusedPanel(e,t){q(t);}arrowDownKey(e){let t=this.findNextPanel(this.findPanel(e.currentTarget));t?this.changeFocusedPanel(e,t):this.onHomeKey(e),e.preventDefault();}arrowUpKey(e){let t=this.findPrevPanel(this.findPanel(e.currentTarget));t?this.changeFocusedPanel(e,t):this.onEndKey(e),e.preventDefault();}onHomeKey(e){let t=this.findFirstPanel();this.changeFocusedPanel(e,t),e.preventDefault();}onEndKey(e){let t=this.findLastPanel();this.changeFocusedPanel(e,t),e.preventDefault();}onEnterKey(e){this.disabled()||this.changeActiveValue(),e.preventDefault();}get dataP(){return this.cn({active:this.active()});}static ɵfac=(()=>{let e;return function(n){return(e||(e=m(o)))(n||o);};})();static ɵcmp=g({type:o,selectors:[["p-accordion-header"],["p-accordionheader"]],contentQueries:function(t,n,i){if(t&1&&re(i,He,5),t&2){let f;ae(f=ce())&&(n.toggleicon=f.first);}},hostVars:13,hostBindings:function(t,n){t&1&&k("click",function(f){return n.onClick(f);})("focus",function(){return n.onFocus();})("keydown",function(f){return n.onKeydown(f);}),t&2&&(u("id",n.id())("aria-expanded",n.active())("aria-controls",n.ariaControls())("aria-disabled",n.disabled())("role","button")("tabindex",n.disabled()?"-1":"0")("data-p-active",n.active())("data-p-disabled",n.disabled())("data-p",n.dataP),d(n.cx("header")),V("user-select","none"));},features:[E([b,{provide:Ce,useExisting:o},{provide:D,useExisting:o}]),A([he,c]),_],ngContentSelectors:U,decls:3,vars:1,consts:[[4,"ngTemplateOutlet","ngTemplateOutletContext"],[4,"ngIf"],[3,"class","pBind",4,"ngIf"],["data-p-icon","chevron-up",3,"class","pBind",4,"ngIf"],[3,"pBind"],["data-p-icon","chevron-up",3,"pBind"],["data-p-icon","chevron-down",3,"pBind",4,"ngIf"],["data-p-icon","chevron-down",3,"pBind"]],template:function(t,n){t&1&&(F(),O(0),oe(1,Ve,1,4)(2,We,2,2)),t&2&&(s(),te(n.toggleicon?1:2));},dependencies:[C,le,ue,me,ge,R,c],encapsulation:2,changeDetection:0});}return o;})(),Cn=(()=>{class o extends I{$pcAccordionContent=r(De,{optional:!0,skipSelf:!0})??void 0;bindDirectiveInstance=r(c,{self:!0});componentName="AccordionContent";onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptm("root"));}pcAccordion=r(T(()=>J));pcAccordionPanel=r(T(()=>Ne));active=p(()=>this.pcAccordionPanel.active());ariaLabelledby=p(()=>`${this.pcAccordion.id()}_accordionheader_${this.pcAccordionPanel.value()}`);id=p(()=>`${this.pcAccordion.id()}_accordioncontent_${this.pcAccordionPanel.value()}`);_componentStyle=r(b);ptParams=p(()=>({context:this.active()}));computedMotionOptions=p(()=>M(M({},this.ptm("motion",this.ptParams())),this.pcAccordion.computedMotionOptions()));static ɵfac=(()=>{let e;return function(n){return(e||(e=m(o)))(n||o);};})();static ɵcmp=g({type:o,selectors:[["p-accordion-content"],["p-accordioncontent"]],hostVars:6,hostBindings:function(t,n){t&2&&(u("id",n.id())("role","region")("data-p-active",n.active())("aria-labelledby",n.ariaLabelledby()),d(n.cx("contentContainer")));},features:[E([b,{provide:De,useExisting:o},{provide:D,useExisting:o}]),A([c]),_],ngContentSelectors:U,decls:4,vars:10,consts:[["name","p-collapsible","hideStrategy","visibility",3,"visible","mountOnEnter","unmountOnLeave","options"],[3,"pBind"]],template:function(t,n){t&1&&(F(),S(0,"p-motion",0)(1,"div",1)(2,"div",1),O(3),P()()()),t&2&&(a("visible",n.active())("mountOnEnter",!1)("unmountOnLeave",!1)("options",n.computedMotionOptions()),s(),d(n.cx("contentWrapper")),a("pBind",n.ptm("contentWrapper",n.ptParams())),s(),d(n.cx("content")),a("pBind",n.ptm("content",n.ptParams())));},dependencies:[C,R,c,ye,be],encapsulation:2,changeDetection:0});}return o;})(),J=(()=>{class o extends I{componentName="Accordion";$pcAccordion=r(Ie,{optional:!0,skipSelf:!0})??void 0;bindDirectiveInstance=r(c,{self:!0});onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptm("root"));}value=Q(void 0);multiple=H(!1,{transform:e=>j(e)});styleClass;expandIcon;collapseIcon;selectOnFocus=H(!1,{transform:e=>j(e)});transitionOptions="400ms cubic-bezier(0.86, 0, 0.07, 1)";motionOptions=H(void 0);computedMotionOptions=p(()=>M(M({},this.ptm("motion")),this.motionOptions()));onClose=new z();onOpen=new z();id=ne(fe("pn_id_"));_componentStyle=r(b);onKeydown(e){switch(e.code){case"ArrowDown":this.onTabArrowDownKey(e);break;case"ArrowUp":this.onTabArrowUpKey(e);break;case"Home":e.shiftKey||this.onTabHomeKey(e);break;case"End":e.shiftKey||this.onTabEndKey(e);break;}}onTabArrowDownKey(e){let t=this.findNextHeaderAction(e.target.parentElement);t?this.changeFocusedTab(t):this.onTabHomeKey(e),e.preventDefault();}onTabArrowUpKey(e){let t=this.findPrevHeaderAction(e.target.parentElement);t?this.changeFocusedTab(t):this.onTabEndKey(e),e.preventDefault();}onTabHomeKey(e){let t=this.findFirstHeaderAction();this.changeFocusedTab(t),e.preventDefault();}changeFocusedTab(e){e&&q(e);}findNextHeaderAction(e,t=!1){let n=t?e:e.nextElementSibling,i=x(n,'[data-pc-section="accordionheader"]');return i?B(i,"data-p-disabled")?this.findNextHeaderAction(i.parentElement):x(i.parentElement,'[data-pc-section="accordionheader"]'):null;}findPrevHeaderAction(e,t=!1){let n=t?e:e.previousElementSibling,i=x(n,'[data-pc-section="accordionheader"]');return i?B(i,"data-p-disabled")?this.findPrevHeaderAction(i.parentElement):x(i.parentElement,'[data-pc-section="accordionheader"]'):null;}findFirstHeaderAction(){let e=this.el.nativeElement.firstElementChild;return this.findNextHeaderAction(e,!0);}findLastHeaderAction(){let e=this.el.nativeElement.lastElementChild;return this.findPrevHeaderAction(e,!0);}onTabEndKey(e){let t=this.findLastHeaderAction();this.changeFocusedTab(t),e.preventDefault();}getBlockableElement(){return this.el.nativeElement.children[0];}updateValue(e){let t=this.value();if(this.multiple()){let n=Array.isArray(t)?[...t]:[],i=n.indexOf(e);i!==-1?n.splice(i,1):n.push(e),this.value.set(n);}else t===e?this.value.set(void 0):this.value.set(e);}static ɵfac=(()=>{let e;return function(n){return(e||(e=m(o)))(n||o);};})();static ɵcmp=g({type:o,selectors:[["p-accordion"]],hostVars:2,hostBindings:function(t,n){t&1&&k("keydown",function(f){return n.onKeydown(f);}),t&2&&d(n.cn(n.cx("root"),n.styleClass));},inputs:{value:[1,"value"],multiple:[1,"multiple"],styleClass:"styleClass",expandIcon:"expandIcon",collapseIcon:"collapseIcon",selectOnFocus:[1,"selectOnFocus"],transitionOptions:"transitionOptions",motionOptions:[1,"motionOptions"]},outputs:{value:"valueChange",onClose:"onClose",onOpen:"onOpen"},features:[E([b,{provide:Ie,useExisting:o},{provide:D,useExisting:o}]),A([c]),_],ngContentSelectors:U,decls:1,vars:0,template:function(t,n){t&1&&(F(),O(0));},dependencies:[C,K,R],encapsulation:2,changeDetection:0});}return o;})();var X={1:"Actiu",2:"Inactiu"},Pn=[{label:X[1],value:1},{label:X[2],value:2}],xn="Informaci\xF3",Mn="La consulta de registres inactius encara no est\xE0 disponible.",wn="Funcionalitat pendent",kn="La restauraci\xF3 de registres encara no est\xE0 implementada.";function Fn(o){return X[o?2:1];}var Se=class o{restore=pe();PrimeIcons=Y;actionsAriaLabel="Obrir les accions del registre inactiu";actions=[{label:"Restaura",icon:Y.REFRESH,command:()=>this.restore.emit()}];static ɵfac=function(e){return new(e||o)();};static ɵcmp=g({type:o,selectors:[["app-restore-record-menu"]],outputs:{restore:"restore"},decls:3,vars:5,consts:[["menu",""],["severity","secondary",3,"onClick","text","icon","ariaLabel"],["appendTo","body",3,"model","popup"]],template:function(e,t){if(e&1){let n=ie();S(0,"p-button",1),k("onClick",function(f){Z(n);let we=de(2);return ee(we.toggle(f));}),P(),h(1,"p-menu",2,0);}e&2&&(a("text",!0)("icon",t.PrimeIcons.ELLIPSIS_H)("ariaLabel",t.actionsAriaLabel),s(),a("model",t.actions)("popup",!0));},dependencies:[ve,Ae],encapsulation:2,changeDetection:0});};var Pe=`
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
`;var Ye={root:()=>["p-progressspinner"],spin:"p-progressspinner-spin",circle:"p-progressspinner-circle"},xe=(()=>{class o extends ${name="progressspinner";style=Pe;classes=Ye;static ɵfac=(()=>{let e;return function(n){return(e||(e=m(o)))(n||o);};})();static ɵprov=L({token:o,factory:o.ɵfac});}return o;})();var Me=new y("PROGRESSSPINNER_INSTANCE"),ro=(()=>{class o extends I{componentName="ProgressSpinner";$pcProgressSpinner=r(Me,{optional:!0,skipSelf:!0})??void 0;bindDirectiveInstance=r(c,{self:!0});styleClass;strokeWidth="2";fill="none";animationDuration="2s";ariaLabel;onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptms(["host","root"]));}_componentStyle=r(xe);static ɵfac=(()=>{let e;return function(n){return(e||(e=m(o)))(n||o);};})();static ɵcmp=g({type:o,selectors:[["p-progressSpinner"],["p-progress-spinner"],["p-progressspinner"]],hostVars:5,hostBindings:function(t,n){t&2&&(u("aria-label",n.ariaLabel)("role","progressbar")("aria-busy",!0),d(n.cn(n.cx("root"),n.styleClass)));},inputs:{styleClass:"styleClass",strokeWidth:"strokeWidth",fill:"fill",animationDuration:"animationDuration",ariaLabel:"ariaLabel"},features:[E([xe,{provide:Me,useExisting:o},{provide:D,useExisting:o}]),A([c]),_],decls:2,vars:10,consts:[["viewBox","25 25 50 50",3,"pBind"],["cx","50","cy","50","r","20","stroke-miterlimit","10",3,"pBind"]],template:function(t,n){t&1&&(w(),S(0,"svg",0),h(1,"circle",1),P()),t&2&&(d(n.cx("spin")),V("animation-duration",n.animationDuration),a("pBind",n.ptm("spin")),s(),d(n.cx("circle")),a("pBind",n.ptm("circle")),u("fill",n.fill)("stroke-width",n.strokeWidth));},dependencies:[C,K,c],encapsulation:2,changeDetection:0});}return o;})();export{X as a,Pn as b,xn as c,Mn as d,wn as e,kn as f,Fn as g,Se as h,ro as i,Ne as j,En as k,Cn as l,J as m};/**i18n:22e098ff1cbf4220c99e0cb12af57cfa6bdd822e90f8e9443f8715589c7ae7f3*/