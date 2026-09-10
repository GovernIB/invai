import{f as K}from"./chunk-YKAKMDYW.js";import{L as q,N as x,T as W,U as a,V as U,ha as J}from"./chunk-Y66MR3DC.js";import{F as P,G as R}from"./chunk-QES7LLTG.js";import{f as j,i as G}from"./chunk-SV67CR7A.js";import{Ab as m,Gc as Q,Hb as F,Ib as A,Oc as y,Pb as k,Pc as $,Qa as g,R as v,Rb as L,S as C,Ub as D,V as T,Vb as N,Wb as h,X as c,Xb as u,aa as p,ba as w,cb as S,cc as H,dc as r,gb as V,ha as _,hb as E,ib as B,lc as O,nc as z,pb as d,ra as b,sb as M,tb as I,yb as s,zb as f}from"./chunk-PM3R7XGD.js";var X=`
    .p-toggleswitch {
        display: inline-block;
        width: dt('toggleswitch.width');
        height: dt('toggleswitch.height');
    }

    .p-toggleswitch-input {
        cursor: pointer;
        appearance: none;
        position: absolute;
        top: 0;
        inset-inline-start: 0;
        width: 100%;
        height: 100%;
        padding: 0;
        margin: 0;
        opacity: 0;
        z-index: 1;
        outline: 0 none;
        border-radius: dt('toggleswitch.border.radius');
    }

    .p-toggleswitch-slider {
        cursor: pointer;
        width: 100%;
        height: 100%;
        border-width: dt('toggleswitch.border.width');
        border-style: solid;
        border-color: dt('toggleswitch.border.color');
        background: dt('toggleswitch.background');
        transition:
            background dt('toggleswitch.transition.duration'),
            color dt('toggleswitch.transition.duration'),
            border-color dt('toggleswitch.transition.duration'),
            outline-color dt('toggleswitch.transition.duration'),
            box-shadow dt('toggleswitch.transition.duration');
        border-radius: dt('toggleswitch.border.radius');
        outline-color: transparent;
        box-shadow: dt('toggleswitch.shadow');
    }

    .p-toggleswitch-handle {
        position: absolute;
        top: 50%;
        display: flex;
        justify-content: center;
        align-items: center;
        background: dt('toggleswitch.handle.background');
        color: dt('toggleswitch.handle.color');
        width: dt('toggleswitch.handle.size');
        height: dt('toggleswitch.handle.size');
        inset-inline-start: dt('toggleswitch.gap');
        margin-block-start: calc(-1 * calc(dt('toggleswitch.handle.size') / 2));
        border-radius: dt('toggleswitch.handle.border.radius');
        transition:
            background dt('toggleswitch.transition.duration'),
            color dt('toggleswitch.transition.duration'),
            inset-inline-start dt('toggleswitch.slide.duration'),
            box-shadow dt('toggleswitch.slide.duration');
    }

    .p-toggleswitch.p-toggleswitch-checked .p-toggleswitch-slider {
        background: dt('toggleswitch.checked.background');
        border-color: dt('toggleswitch.checked.border.color');
    }

    .p-toggleswitch.p-toggleswitch-checked .p-toggleswitch-handle {
        background: dt('toggleswitch.handle.checked.background');
        color: dt('toggleswitch.handle.checked.color');
        inset-inline-start: calc(dt('toggleswitch.width') - calc(dt('toggleswitch.handle.size') + dt('toggleswitch.gap')));
    }

    .p-toggleswitch:not(.p-disabled):has(.p-toggleswitch-input:hover) .p-toggleswitch-slider {
        background: dt('toggleswitch.hover.background');
        border-color: dt('toggleswitch.hover.border.color');
    }

    .p-toggleswitch:not(.p-disabled):has(.p-toggleswitch-input:hover) .p-toggleswitch-handle {
        background: dt('toggleswitch.handle.hover.background');
        color: dt('toggleswitch.handle.hover.color');
    }

    .p-toggleswitch:not(.p-disabled):has(.p-toggleswitch-input:hover).p-toggleswitch-checked .p-toggleswitch-slider {
        background: dt('toggleswitch.checked.hover.background');
        border-color: dt('toggleswitch.checked.hover.border.color');
    }

    .p-toggleswitch:not(.p-disabled):has(.p-toggleswitch-input:hover).p-toggleswitch-checked .p-toggleswitch-handle {
        background: dt('toggleswitch.handle.checked.hover.background');
        color: dt('toggleswitch.handle.checked.hover.color');
    }

    .p-toggleswitch:not(.p-disabled):has(.p-toggleswitch-input:focus-visible) .p-toggleswitch-slider {
        box-shadow: dt('toggleswitch.focus.ring.shadow');
        outline: dt('toggleswitch.focus.ring.width') dt('toggleswitch.focus.ring.style') dt('toggleswitch.focus.ring.color');
        outline-offset: dt('toggleswitch.focus.ring.offset');
    }

    .p-toggleswitch.p-invalid > .p-toggleswitch-slider {
        border-color: dt('toggleswitch.invalid.border.color');
    }

    .p-toggleswitch.p-disabled {
        opacity: 1;
    }

    .p-toggleswitch.p-disabled .p-toggleswitch-slider {
        background: dt('toggleswitch.disabled.background');
    }

    .p-toggleswitch.p-disabled .p-toggleswitch-handle {
        background: dt('toggleswitch.handle.disabled.background');
    }
`;var te=["handle"],ie=["input"],ne=t=>({checked:t});function oe(t,ee){t&1&&F(0)}function le(t,ee){if(t&1&&B(0,oe,1,0,"ng-container",3),t&2){let i=L();s("ngTemplateOutlet",i.handleTemplate||i._handleTemplate)("ngTemplateOutletContext",z(2,ne,i.checked()))}}var de=`
    ${X}

    p-toggleswitch.ng-invalid.ng-dirty > .p-toggleswitch-slider {
        border-color: dt('toggleswitch.invalid.border.color');
    }
`,se={root:{position:"relative"}},re={root:({instance:t})=>["p-toggleswitch p-component",{"p-toggleswitch p-component":!0,"p-toggleswitch-checked":t.checked(),"p-disabled":t.$disabled(),"p-invalid":t.invalid()}],input:"p-toggleswitch-input",slider:"p-toggleswitch-slider",handle:"p-toggleswitch-handle"},Y=(()=>{class t extends q{name="toggleswitch";style=de;classes=re;inlineStyles=se;static \u0275fac=(()=>{let i;return function(e){return(i||(i=b(t)))(e||t)}})();static \u0275prov=C({token:t,factory:t.\u0275fac})}return t})();var Z=new T("TOGGLESWITCH_INSTANCE"),ae={provide:J,useExisting:v(()=>ce),multi:!0},ce=(()=>{class t extends K{componentName="ToggleSwitch";$pcToggleSwitch=c(Z,{optional:!0,skipSelf:!0})??void 0;bindDirectiveInstance=c(a,{self:!0});onAfterViewChecked(){this.bindDirectiveInstance.setAttrs(this.ptms(["host","root"]))}styleClass;tabindex;inputId;readonly;trueValue=!0;falseValue=!1;ariaLabel;size=Q();ariaLabelledBy;autofocus;onChange=new _;input;handleTemplate;_handleTemplate;focused=!1;_componentStyle=c(Y);templates;onHostClick(i){this.onClick(i)}onAfterContentInit(){this.templates.forEach(i=>{i.getType()==="handle"?this._handleTemplate=i.template:this._handleTemplate=i.template})}onClick(i){!this.$disabled()&&!this.readonly&&(this.writeModelValue(this.checked()?this.falseValue:this.trueValue),this.onModelChange(this.modelValue()),this.onChange.emit({originalEvent:i,checked:this.modelValue()}),this.input.nativeElement.focus())}onFocus(){this.focused=!0}onBlur(){this.focused=!1,this.onModelTouched()}checked(){return this.modelValue()===this.trueValue}writeControlValue(i,n){n(i),this.cd.markForCheck()}get dataP(){return this.cn({checked:this.checked(),disabled:this.$disabled(),invalid:this.invalid()})}static \u0275fac=(()=>{let i;return function(e){return(i||(i=b(t)))(e||t)}})();static \u0275cmp=S({type:t,selectors:[["p-toggleswitch"],["p-toggleSwitch"],["p-toggle-switch"]],contentQueries:function(n,e,o){if(n&1&&D(o,te,4)(o,P,4),n&2){let l;h(l=u())&&(e.handleTemplate=l.first),h(l=u())&&(e.templates=l)}},viewQuery:function(n,e){if(n&1&&N(ie,5),n&2){let o;h(o=u())&&(e.input=o.first)}},hostVars:7,hostBindings:function(n,e){n&1&&k("click",function(l){return e.onHostClick(l)}),n&2&&(d("data-p-checked",e.checked())("data-p-disabled",e.$disabled())("data-p",e.dataP),H(e.sx("root")),r(e.cn(e.cx("root"),e.styleClass)))},inputs:{styleClass:"styleClass",tabindex:[2,"tabindex","tabindex",$],inputId:"inputId",readonly:[2,"readonly","readonly",y],trueValue:"trueValue",falseValue:"falseValue",ariaLabel:"ariaLabel",size:[1,"size"],ariaLabelledBy:"ariaLabelledBy",autofocus:[2,"autofocus","autofocus",y]},outputs:{onChange:"onChange"},features:[O([ae,Y,{provide:Z,useExisting:t},{provide:x,useExisting:t}]),V([a]),E],decls:5,vars:22,consts:[["input",""],["type","checkbox","role","switch",3,"focus","blur","checked","pAutoFocus","pBind"],[3,"pBind"],[4,"ngTemplateOutlet","ngTemplateOutletContext"]],template:function(n,e){if(n&1){let o=A();f(0,"input",1,0),k("focus",function(){return p(o),w(e.onFocus())})("blur",function(){return p(o),w(e.onBlur())}),m(),f(2,"div",2)(3,"div",2),M(4,le,1,4,"ng-container"),m()()}n&2&&(r(e.cx("input")),s("checked",e.checked())("pAutoFocus",e.autofocus)("pBind",e.ptm("input")),d("id",e.inputId)("required",e.required()?"":void 0)("disabled",e.$disabled()?"":void 0)("aria-checked",e.checked())("aria-labelledby",e.ariaLabelledBy)("aria-label",e.ariaLabel)("name",e.name())("tabindex",e.tabindex),g(2),r(e.cx("slider")),s("pBind",e.ptm("slider")),d("data-p",e.dataP),g(),r(e.cx("handle")),s("pBind",e.ptm("handle")),d("data-p",e.dataP),g(),I(e.handleTemplate||e._handleTemplate?4:-1))},dependencies:[G,j,W,R,U,a],encapsulation:2,changeDetection:0})}return t})();export{ce as a};
/**i18n:85b369cd519586902b7f4f17a6e24b5cb5057fd4d248a08016454ba7f85e36fe*/
